package org.channel.ensharponlinejudge.auth.infra.github;

import java.util.Map;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GithubOAuthClient {

  @Value("${github.client-id:defaultId}")
  private String clientId;

  @Value("${github.client-secret:defaultSecret}")
  private String clientSecret;

  private final RestTemplate restTemplate = new RestTemplate();

  public String getAccessToken(String code) {
    String url = "https://github.com/login/oauth/access_token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

    Map<String, String> body =
        Map.of(
            "client_id", clientId,
            "client_secret", clientSecret,
            "code", code);

    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

    if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
      throw new BusinessException(AuthErrorCode.GITHUB_AUTH_FAILED);
    }

    return (String) response.getBody().get("access_token");
  }

  public Map<String, Object> getUserInfo(String accessToken) {
    String url = "https://api.github.com/user";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

    if (response.getBody() == null || !response.getBody().containsKey("login")) {
      throw new BusinessException(AuthErrorCode.GITHUB_AUTH_FAILED);
    }

    return response.getBody();
  }

  @Getter
  @RequiredArgsConstructor
  public static class GithubProfile {
    private final String id;
    private final String login;
    private final String name;
    private final String avatarUrl;
  }
}
