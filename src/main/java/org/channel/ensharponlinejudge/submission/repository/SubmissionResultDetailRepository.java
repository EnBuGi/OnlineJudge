package org.channel.ensharponlinejudge.submission.repository;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionResultDetailRepository extends JpaRepository<SubmissionResultDetail, UUID> {
  List<SubmissionResultDetail> findBySubmissionId(UUID submissionId);
}
