package org.channel.ensharponlinejudge.submission.repository;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

  boolean existsByUserIdAndProjectIdAndStatusIn(
      UUID userId, UUID projectId, List<SubmissionStatus> statuses);

  List<Submission> findByProjectId(UUID projectId);

  List<Submission> findByUserIdAndProjectIdOrderBySubmittedAtDesc(UUID userId, UUID projectId);

  List<Submission> findAllByOrderBySubmittedAtDesc();
}
