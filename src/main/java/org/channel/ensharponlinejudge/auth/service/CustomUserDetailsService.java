package org.channel.ensharponlinejudge.auth.service;

import java.util.List;
import java.util.stream.Collectors;

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
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    List<GrantedAuthority> grantedAuthorities =
        member.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(Collectors.toList());

    // Spring Security는 UserDetails 인터페이스의 구현체로 사용자 정보를 다룹니다.
    return new org.springframework.security.core.userdetails.User(member.getEmail(), member.getPassword(), grantedAuthorities);
  }
}
