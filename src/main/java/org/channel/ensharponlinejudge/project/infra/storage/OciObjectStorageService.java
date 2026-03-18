package org.channel.ensharponlinejudge.project.infra.storage;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.channel.ensharponlinejudge.global.config.OciProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciObjectStorageService implements ObjectStorageService {

  private static final String ALLOWED_CONTENT_TYPE = "application/zip";
  private static final String[] TEST_DIRECTORY_PREFIXES = {"src/test/", "src\\test\\"};
  // Zip Bomb 방어 상수
  private static final int MAX_ENTRY_COUNT = 5_000;
  private static final long MAX_TOTAL_UNCOMPRESSED_SIZE = 100L * 1024 * 1024; // 100 MB
  private static final long MAX_SINGLE_ENTRY_SIZE = 50L * 1024 * 1024; // 50 MB

  private final ObjectStorageClient objectStorageClient;
  private final OciProperties ociProperties;

  @Override
  public void uploadTestCode(MultipartFile file, String objectKey) {
    validateZipFile(file);
    validateTestDirectoryExists(file);

    try {
      PutObjectRequest request =
          PutObjectRequest.builder()
              .namespaceName(ociProperties.getNamespace())
              .bucketName(ociProperties.getTestCodeBucketName())
              .objectName(objectKey)
              .contentType(ALLOWED_CONTENT_TYPE)
              .putObjectBody(file.getInputStream())
              .contentLength(file.getSize())
              .build();

      objectStorageClient.putObject(request);
      log.info("[ObjectStorage] 업로드 완료: objectKey={}", objectKey);

    } catch (IOException | BmcException e) {
      log.error("[ObjectStorage] 업로드 실패: objectKey={}, error={}", objectKey, e.getMessage(), e);
      throw new IllegalStateException("테스트 코드 파일 업로드에 실패했습니다.", e);
    }
  }

  @Override
  public String generateGetUrl(String objectKey) {
    try {
      ZonedDateTime expiresAt =
          ZonedDateTime.now(ZoneOffset.UTC).plusHours(ociProperties.getParExpirationHours());

      CreatePreauthenticatedRequestDetails details =
          CreatePreauthenticatedRequestDetails.builder()
              // PAR 이름에 '/'가 있으면 OCI가 계층으로 해석할 수 있으므로 치환
              .name("par-" + objectKey.replace('/', '_'))
              .objectName(objectKey)
              .accessType(
                  com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType
                      .ObjectRead)
              .timeExpires(java.util.Date.from(expiresAt.toInstant()))
              .build();

      CreatePreauthenticatedRequestRequest request =
          CreatePreauthenticatedRequestRequest.builder()
              .namespaceName(ociProperties.getNamespace())
              .bucketName(ociProperties.getTestCodeBucketName())
              .createPreauthenticatedRequestDetails(details)
              .build();

      CreatePreauthenticatedRequestResponse response =
          objectStorageClient.createPreauthenticatedRequest(request);

      String accessUri = response.getPreauthenticatedRequest().getAccessUri();
      log.info("[ObjectStorage] PAR URL 생성 완료: objectKey={}", objectKey);

      return "https://objectstorage." + ociProperties.getRegion() + ".oraclecloud.com" + accessUri;

    } catch (BmcException e) {
      log.error("[ObjectStorage] PAR 생성 실패: objectKey={}, error={}", objectKey, e.getMessage(), e);
      throw new IllegalStateException("테스트 코드 다운로드 URL 생성에 실패했습니다.", e);
    }
  }

  /** 파일이 .zip 형식인지 검증합니다. */
  private void validateZipFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("파일이 비어있습니다.");
    }
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
      throw new IllegalArgumentException("테스트 코드 파일은 .zip 형식이어야 합니다.");
    }
  }

  /**
   * .zip 파일 내부에 src/test 디렉토리가 존재하는지 검증합니다.
   *
   * <p>동시에 다음 보안 위협을 방어합니다:
   *
   * <ul>
   *   <li>Zip Bomb: 엔트리 수, 압축 해제 크기 초과 시 거부
   *   <li>Path Traversal: '../' 가 포함된 엔트리명 거부
   * </ul>
   */
  private void validateTestDirectoryExists(MultipartFile file) {
    boolean testDirFound = false;
    int entryCount = 0;
    long totalUncompressedSize = 0;

    try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {

        // Path Traversal 방어
        String entryName = entry.getName();
        if (entryName.contains("..") || entryName.startsWith("/")) {
          throw new IllegalArgumentException("업로드된 .zip 파일에 허용되지 않는 경로가 포함되어 있습니다: " + entryName);
        }

        // Zip Bomb: 엔트리 수 제한
        if (++entryCount > MAX_ENTRY_COUNT) {
          throw new IllegalArgumentException(
              "업로드된 .zip 파일의 엔트리 수가 허용 한도(" + MAX_ENTRY_COUNT + "개)를 초과합니다.");
        }

        // Zip Bomb: 단일 엔트리 크기 제한
        long entrySize = entry.getSize();
        if (entrySize > MAX_SINGLE_ENTRY_SIZE) {
          throw new IllegalArgumentException(
              "업로드된 .zip 파일의 단일 파일 크기가 허용 한도("
                  + (MAX_SINGLE_ENTRY_SIZE / 1024 / 1024)
                  + "MB)를 초과합니다.");
        }

        // Zip Bomb: 전체 압축 해제 크기 제한 (entrySize가 -1이면 알 수 없음 → 실제 읽어서 체크)
        if (entrySize >= 0) {
          totalUncompressedSize += entrySize;
        } else {
          // 크기 메타데이터가 없는 항목은 실제로 읽으며 카운트
          byte[] buf = new byte[8192];
          int read;
          while ((read = zis.read(buf)) != -1) {
            totalUncompressedSize += read;
            if (totalUncompressedSize > MAX_TOTAL_UNCOMPRESSED_SIZE) break;
          }
        }
        if (totalUncompressedSize > MAX_TOTAL_UNCOMPRESSED_SIZE) {
          throw new IllegalArgumentException(
              "업로드된 .zip 파일의 압축 해제 크기가 허용 한도("
                  + (MAX_TOTAL_UNCOMPRESSED_SIZE / 1024 / 1024)
                  + "MB)를 초과합니다.");
        }

        // src/test 디렉토리 검증
        if (!testDirFound) {
          for (String prefix : TEST_DIRECTORY_PREFIXES) {
            if (entryName.contains(prefix)) {
              testDirFound = true;
              break;
            }
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(".zip 파일을 읽을 수 없습니다.", e);
    }

    if (!testDirFound) {
      throw new IllegalArgumentException(
          "업로드된 .zip 파일에 'src/test' 디렉토리가 존재하지 않습니다. "
              + "채점 코드는 반드시 'src/test' 또는 그 하위 경로에 위치해야 합니다.");
    }
  }
}
