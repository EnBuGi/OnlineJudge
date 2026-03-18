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
   * 지정된 오브젝트에 대한 읽기 전용 사전 인증 요청(PAR) URL을 생성합니다.
   *
   * @param objectKey 버킷 내 오브젝트 키
   * @return PAR GET URL 문자열
   */
  String generateGetUrl(String objectKey);
}
