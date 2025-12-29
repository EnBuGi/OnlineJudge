package org.channel.ensharponlinejudge.health;

import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "🏥 Health Check", description = "서버 및 데이터베이스 상태 확인 API")
@RestController
public class HealthController {

  @PersistenceContext private EntityManager entityManager;

  @Operation(
      summary = "서버 상태 확인",
      description = "서버와 데이터베이스의 헬스 체크를 수행합니다. 데이터베이스 연결 상태를 포함한 전체 시스템의 상태를 반환합니다.")
  @GetMapping
  public ResponseEntity<?> healthCheck() {
    try {
      Query query = entityManager.createNativeQuery("SELECT 1");
      query.getSingleResult();

      return ResponseEntity.status(HttpStatus.OK)
          .body(
              Map.of(
                  "status", "UP",
                  "db", "UP"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "status", "DOWN",
                  "db", "DOWN",
                  "message", e.getMessage()));
    }
  }
}
