package org.channel.ensharponlinejudge.domain.member.repository;

import org.channel.ensharponlinejudge.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  boolean existsByEmail(String email);
}
