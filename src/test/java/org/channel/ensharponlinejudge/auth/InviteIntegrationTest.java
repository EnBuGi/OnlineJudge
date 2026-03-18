package org.channel.ensharponlinejudge.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.channel.ensharponlinejudge.auth.controller.dto.InviteRequest;
import org.channel.ensharponlinejudge.auth.controller.dto.InviteResponse;
import org.channel.ensharponlinejudge.support.ApiTestBase;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class InviteIntegrationTest extends ApiTestBase {

  @Test
  @DisplayName("유효한 초대 토큰으로 검증 요청 시 토큰 정보를 반환한다.")
  void validateInvite_ValidToken_ReturnsInfo() {
    // 1. 초대 토큰 생성
    InviteRequest inviteRequest = new InviteRequest(Role.MENTEE, 3600000L);
    InviteResponse inviteResponse =
        given()
            .log()
            .all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(inviteRequest)
            .when()
            .post("/api/v1/admin/invites")
            .then()
            .log()
            .all()
            .extract()
            .as(InviteResponse.class);

    // 2. 토큰 검증
    given()
        .queryParam("token", inviteResponse.token())
        .when()
        .get("/api/v1/invites/validate")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("token", equalTo(inviteResponse.token()))
        .body("role", equalTo(Role.MENTEE.name()))
        .body("generation", equalTo(inviteResponse.generation()));
  }

  @Test
  @DisplayName("유효하지 않은 초대 토큰으로 검증 요청 시 400 에러를 반환한다.")
  void validateInvite_InvalidToken_ReturnsError() {
    given()
        .queryParam("token", "invalid-token")
        .when()
        .get("/api/v1/invites/validate")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("만료된 초대 토큰으로 검증 요청 시 400 에러를 반환한다.")
  void validateInvite_ExpiredToken_ReturnsError() throws InterruptedException {
    // 1. 초대 토큰 생성 (1ms 만료)
    InviteRequest inviteRequest = new InviteRequest(Role.MENTOR, 1L);
    InviteResponse inviteResponse =
        given()
            .log()
            .all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(inviteRequest)
            .when()
            .post("/api/v1/admin/invites")
            .then()
            .log()
            .all()
            .extract()
            .as(InviteResponse.class);

    // 2ms 대기
    Thread.sleep(100);

    // 2. 토큰 검증
    given()
        .queryParam("token", inviteResponse.token())
        .when()
        .get("/api/v1/invites/validate")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }
}
