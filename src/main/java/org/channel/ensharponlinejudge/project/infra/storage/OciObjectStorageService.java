package org.channel.ensharponlinejudge.project.infra.storage;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.ProjectErrorCode;
import org.channel.ensharponlinejudge.global.config.OciProperties;
import org.channel.ensharponlinejudge.project.infra.ZipValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CopyObjectRequest;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciObjectStorageService implements ObjectStorageService {

  private static final String ALLOWED_CONTENT_TYPE = "application/zip";

  private final ObjectStorageClient objectStorageClient;
  private final OciProperties ociProperties;
  private final ZipValidator zipValidator;

  @Override
  public void uploadTestCode(MultipartFile file, String objectKey) {
    zipValidator.validateTestCodeZip(file);
    putObject(file, objectKey);
  }

  @Override
  public String uploadTempTestCode(MultipartFile file) {
    zipValidator.validateTestCodeZip(file);
    String tempKey = "temp/" + UUID.randomUUID() + ".zip";
    putObject(file, tempKey);
    return tempKey;
  }

  @Override
  public void moveTempToPermanent(String tempKey, String permanentKey) {
    try {
      // OCI CopyObject
      CopyObjectRequest copyRequest =
          CopyObjectRequest.builder()
              .namespaceName(ociProperties.getNamespace())
              .bucketName(ociProperties.getTestCodeBucketName())
              .copyObjectDetails(
                  com.oracle.bmc.objectstorage.model.CopyObjectDetails.builder()
                      .sourceObjectName(tempKey)
                      .destinationObjectName(permanentKey)
                      .destinationRegion(ociProperties.getRegion())
                      .destinationBucket(ociProperties.getTestCodeBucketName())
                      .destinationNamespace(ociProperties.getNamespace())
                      .build())
              .build();

      objectStorageClient.copyObject(copyRequest);
      log.info("[ObjectStorage] 파일 이동 완료: {} -> {}", tempKey, permanentKey);

      // Delete Temp
      DeleteObjectRequest deleteRequest =
          DeleteObjectRequest.builder()
              .namespaceName(ociProperties.getNamespace())
              .bucketName(ociProperties.getTestCodeBucketName())
              .objectName(tempKey)
              .build();
      objectStorageClient.deleteObject(deleteRequest);

    } catch (BmcException e) {
      if (e.getStatusCode() == 404) {
        throw new BusinessException(ProjectErrorCode.ERR_TEMP_FILE_NOT_FOUND);
      }
      log.error("[ObjectStorage] 파일 이동 실패: tempKey={}, error={}", tempKey, e.getMessage(), e);
      throw new IllegalStateException("테스트 코드 파일 처리에 실패했습니다.", e);
    }
  }

  private void putObject(MultipartFile file, String objectKey) {
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
}
