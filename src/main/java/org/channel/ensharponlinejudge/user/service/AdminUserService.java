package org.channel.ensharponlinejudge.user.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.channel.ensharponlinejudge.user.domain.Role;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.presentation.dto.AdminUserResponse;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

  private final UserRepository userRepository;

  public List<AdminUserResponse> getUsers(Integer generation) {
    return (generation == null
            ? userRepository.findAll()
            : userRepository.findByGeneration(generation))
        .stream()
            .map(
                user ->
                    AdminUserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .githubId(user.getGithubId())
                        .role(user.getRole())
                        .generation(user.getGeneration())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
            .collect(Collectors.toList());
  }

  @Transactional
  public void updateUserRole(UUID userId, Role role) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    user.updateRole(role);
  }
}
