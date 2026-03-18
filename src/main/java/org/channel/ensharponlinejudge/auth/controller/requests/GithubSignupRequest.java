package org.channel.ensharponlinejudge.auth.controller.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GithubSignupRequest {
  private String githubId;
  private String name;
  private int generation;
  private String profileImageUrl;
  private String inviteToken;
}
