package org.channel.ensharponlinejudge.auth.service;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository memberRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
    User member;
    try {
      member =
          memberRepository
              .findById(UUID.fromString(subject))
              .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + subject));
    } catch (IllegalArgumentException e) {
      throw new UsernameNotFoundException("유효한 UUID가 아닙니다: " + subject);
    }

    List<GrantedAuthority> grantedAuthorities =
        List.of(new SimpleGrantedAuthority(member.getRole().name()));

    return new org.springframework.security.core.userdetails.User(
        member.getId().toString(), "", grantedAuthorities);
  }
}
