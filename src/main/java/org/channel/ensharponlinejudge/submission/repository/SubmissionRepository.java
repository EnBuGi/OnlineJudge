package org.channel.ensharponlinejudge.submission.repository;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

  void deleteByProjectId(UUID projectId);

  boolean existsByUserIdAndProjectIdAndStatusIn(
      UUID userId, UUID projectId, List<SubmissionStatus> statuses);

  List<Submission> findByProjectId(UUID projectId);

  Page<Submission> findByUserIdAndProjectIdOrderBySubmittedAtDesc(
      UUID userId, UUID projectId, Pageable pageable);

  List<Submission> findByUserIdAndProjectIdOrderBySubmittedAtDesc(UUID userId, UUID projectId);

  Page<Submission> findByUserId(UUID userId, Pageable pageable);

  Page<Submission> findByUserIdOrderBySubmittedAtDesc(UUID userId, Pageable pageable);

  List<Submission> findByUserIdOrderBySubmittedAtDesc(UUID userId);

  Page<Submission> findAll(Pageable pageable);

  Page<Submission> findAllByOrderBySubmittedAtDesc(Pageable pageable);

  List<Submission> findAllByOrderBySubmittedAtDesc();
}
