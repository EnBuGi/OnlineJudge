package org.channel.ensharponlinejudge.project.repository;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.ProjectTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTestCaseRepository extends JpaRepository<ProjectTestCase, UUID> {
  List<ProjectTestCase> findByProjectId(UUID projectId);

  void deleteByProjectId(UUID projectId);
}
