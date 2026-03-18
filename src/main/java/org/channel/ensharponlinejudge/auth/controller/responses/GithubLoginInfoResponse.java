package org.channel.ensharponlinejudge.auth.controller.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubLoginInfoResponse {
  private String message;
  private String githubId;
  private String profileImageUrl;
}
