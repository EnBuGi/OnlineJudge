package org.channel.ensharponlinejudge.auth;

import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.detailedCookie;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.channel.ensharponlinejudge.auth.controller.dto.InviteRequest;
import org.channel.ensharponlinejudge.auth.controller.dto.InviteResponse;
import org.channel.ensharponlinejudge.auth.controller.requests.GithubLoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.GithubSignupRequest;
import org.channel.ensharponlinejudge.auth.infra.github.GithubOAuthClient;
import org.channel.ensharponlinejudge.support.ApiTestBase;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.http.Cookie;
import io.restassured.response.Response;

public class AuthIntegrationTest extends ApiTestBase {

  @MockBean private GithubOAuthClient githubOAuthClient;

  private String getValidState() {
    return given()
        .queryParam("redirectUri", "http://localhost:3000/auth/github/callback")
        .when()
        .get("/api/v1/auth/login/github/url")
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .asString()
        .split("state=")[1]
        .split("&")[0];
  }

  @Test
  @DisplayName("GitHub 로그인 시 새로운 사용자면 404와 가입 정보를 반환한다.")
  void loginGithub_NewUser_ReturnsSignupInfo() {
    // Given
    when(githubOAuthClient.getAccessToken(anyString())).thenReturn("test-access-token");
    when(githubOAuthClient.getUserInfo(anyString()))
        .thenReturn(
            Map.of(
                "id", "12345", "avatar_url", "https://github.com/avatar.png", "login", "testuser"));

    String state = getValidState();
    GithubLoginRequest request = new GithubLoginRequest("test-code", state);

    // When & Then
    given()
        .log()
        .all()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(request)
        .when()
        .post("/api/v1/auth/login/github")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body("githubId", equalTo("12345"))
        .body("profileImageUrl", equalTo("https://github.com/avatar.png"));
  }

  @Test
  @DisplayName("GitHub 회원가입 후 로그인하면 토큰을 반환한다.")
  void signupAndLogin_Success() {
    // 1. 초대 토큰 생성 (MENTOR)
    InviteRequest inviteRequest = new InviteRequest(Role.MENTOR, 3600000L);
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

    // 2. 회원가입
    GithubSignupRequest signupRequest =
        new GithubSignupRequest(
            "12345", "홍길동", 20, "https://github.com/avatar.png", inviteResponse.token());

    given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(signupRequest)
        .when()
        .post("/api/v1/auth/signup/github")
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .body("accessToken", notNullValue())
        .cookie("refresh_token", notNullValue());

    // 3. 로그인
    when(githubOAuthClient.getAccessToken(anyString())).thenReturn("test-access-token");
    when(githubOAuthClient.getUserInfo(anyString()))
        .thenReturn(
            Map.of(
                "id", "12345", "avatar_url", "https://github.com/avatar.png", "login", "testuser"));

    String state = getValidState();
    GithubLoginRequest loginRequest = new GithubLoginRequest("test-code", state);

    given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(loginRequest)
        .when()
        .post("/api/v1/auth/login/github")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("accessToken", notNullValue())
        .cookie("refresh_token", notNullValue());
  }

  @Test
  @DisplayName("토큰 재발급 및 로그아웃 테스트")
  void reissueAndLogout_Success() {
    // 1. 회원가입 (MENTEE)
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

    GithubSignupRequest signupRequest =
        new GithubSignupRequest(
            "67890",
            "성춘향",
            inviteResponse.generation(),
            "https://github.com/avatar.png",
            inviteResponse.token());

    Response signupResponse =
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(signupRequest)
            .when()
            .post("/api/v1/auth/signup/github");

    String accessToken = signupResponse.jsonPath().getString("accessToken");
    Cookie refreshTokenCookie = signupResponse.getDetailedCookie("refresh_token");

    // 2. 토큰 재발급
    given()
        .cookie(refreshTokenCookie)
        .when()
        .post("/api/v1/auth/token/reissue")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("accessToken", notNullValue())
        .cookie("refresh_token", notNullValue());

    // 3. 로그아웃
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .delete("/api/v1/auth/token")
        .then()
        .statusCode(HttpStatus.OK.value())
        .cookie("refresh_token", detailedCookie().maxAge(0));
  }
}
