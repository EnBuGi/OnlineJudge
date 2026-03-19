package org.channel.ensharponlinejudge.project.infra;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.ProjectErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ZipValidator {

  private static final String TEST_DIRECTORY_PREFIX = "src/test/";
  // Zip Bomb 방어 상수
  private static final int MAX_ENTRY_COUNT = 5_000;
  private static final long MAX_TOTAL_UNCOMPRESSED_SIZE = 100L * 1024 * 1024; // 100 MB
  private static final long MAX_SINGLE_ENTRY_SIZE = 50L * 1024 * 1024; // 50 MB
  private static final long MAX_COMPRESSED_SIZE = 10L * 1024 * 1024; // 10 MB

  public void validateTestCodeZip(MultipartFile file) {
    validateZipFile(file);
    validateStrictTestDirectory(file);
  }

  private void validateZipFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("파일이 비어있습니다.");
    }

    if (file.getSize() > MAX_COMPRESSED_SIZE) {
      throw new BusinessException(ProjectErrorCode.ERR_FILE_SIZE_EXCEEDED);
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
      throw new BusinessException(ProjectErrorCode.ERR_INVALID_FILE_EXTENSION);
    }
  }

  private void validateStrictTestDirectory(MultipartFile file) {
    boolean testDirFound = false;
    int entryCount = 0;
    long totalUncompressedSize = 0;

    try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();

        // Metadata files and folders (ignore them)
        if (entryName.startsWith(".")
            || entryName.contains("/.")
            || entryName.startsWith("__MACOSX")
            || entryName.contains("/__MACOSX")) {
          continue;
        }

        // Path Traversal 방어
        if (entryName.contains("..") || entryName.startsWith("/")) {
          throw new IllegalArgumentException("업로드된 .zip 파일에 허용되지 않는 경로가 포함되어 있습니다: " + entryName);
        }

        // Strict Structure Check: Only src/test/ is allowed (and hidden files like .DS_Store are
        // ignored but shouldn't be at root)
        // Allow directories like "src/" as long as they eventually lead to "src/test/"
        // But more specifically, any file must be under src/test/
        if (!entry.isDirectory()) {
          if (!entryName.startsWith(TEST_DIRECTORY_PREFIX)) {
            throw new BusinessException(ProjectErrorCode.ERR_INVALID_FILE_STRUCTURE);
          }
        } else {
          // It's a directory entry. It must be "src/", "src/test/", or "src/test/..."
          if (!entryName.startsWith("src/") && !entryName.equals("src")) {
            throw new BusinessException(ProjectErrorCode.ERR_INVALID_FILE_STRUCTURE);
          }
          if (entryName.startsWith("src/")
              && !entryName.startsWith(TEST_DIRECTORY_PREFIX)
              && !entryName.equals("src/")) {
            throw new BusinessException(ProjectErrorCode.ERR_INVALID_FILE_STRUCTURE);
          }
        }

        if (entryName.startsWith(TEST_DIRECTORY_PREFIX)) {
          testDirFound = true;
        }

        // Zip Bomb: 엔트리 수 제한
        if (++entryCount > MAX_ENTRY_COUNT) {
          throw new BusinessException(ProjectErrorCode.ERR_FILE_SIZE_EXCEEDED);
        }

        // Zip Bomb: 단일 엔트리 크기 제한
        long entrySize = entry.getSize();
        if (entrySize > MAX_SINGLE_ENTRY_SIZE) {
          throw new BusinessException(ProjectErrorCode.ERR_FILE_SIZE_EXCEEDED);
        }

        // Zip Bomb: 전체 압축 해제 크기 제한
        if (entrySize >= 0) {
          totalUncompressedSize += entrySize;
        } else {
          byte[] buf = new byte[8192];
          int read;
          while ((read = zis.read(buf)) != -1) {
            totalUncompressedSize += read;
            if (totalUncompressedSize > MAX_TOTAL_UNCOMPRESSED_SIZE) break;
          }
        }
        if (totalUncompressedSize > MAX_TOTAL_UNCOMPRESSED_SIZE) {
          throw new BusinessException(ProjectErrorCode.ERR_FILE_SIZE_EXCEEDED);
        }
      }
    } catch (IOException e) {
      throw new BusinessException(ProjectErrorCode.ERR_FILE_PARSE_FAILED);
    }

    if (!testDirFound) {
      throw new BusinessException(ProjectErrorCode.ERR_INVALID_FILE_STRUCTURE);
    }
  }
}
