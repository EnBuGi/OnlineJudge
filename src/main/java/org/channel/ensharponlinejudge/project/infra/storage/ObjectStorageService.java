package org.channel.ensharponlinejudge.project.infra.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {

  /**
   * .zip 파일을 오브젝트 스토리지에 업로드합니다.
   *
   * @param file 업로드할 .zip 파일
   * @param objectKey 버킷 내 고유 키 (예: 프로젝트 UUID)
   */
  void uploadTestCode(MultipartFile file, String objectKey);

  /**
   * .zip 파일을 오브젝트 스토리지의 임시 경로(temp/)에 업로드합니다.
   *
   * @param file 업로드할 .zip 파일
   * @return 생성된 임시 파일의 키 (UUID 등)
   */
  String uploadTempTestCode(MultipartFile file);

  /**
   * 임시 경로의 파일을 프로젝트 정식 경로로 이동시킵니다.
   *
   * @param tempKey 임시 파일의 키
   * @param permanentKey 프로젝트 정식 경로 내 오브젝트 키
   */
  void moveTempToPermanent(String tempKey, String permanentKey);

  /**
   * 지정된 오브젝트에 대한 읽기 전용 사전 인증 요청(PAR) URL을 생성합니다.
   *
   * @param objectKey 버킷 내 오브젝트 키
   * @return PAR GET URL 문자열
   */
  String generateGetUrl(String objectKey);
}
