package org.channel.ensharponlinejudge.auth.controller.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GithubLoginRequest {
  private String code;
  private String state;
}
