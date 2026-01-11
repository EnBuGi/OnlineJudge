package org.channel.ensharponlinejudge.domain.member.repository;

import java.util.UUID;

import org.channel.ensharponlinejudge.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {
  boolean existsByEmail(String email);

  Member findByEmail(String email);
}
