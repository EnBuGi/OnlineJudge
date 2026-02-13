package org.channel.ensharponlinejudge.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.channel.ensharponlinejudge.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);
}
