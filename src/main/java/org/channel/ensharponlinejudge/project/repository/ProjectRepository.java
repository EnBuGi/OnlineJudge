package org.channel.ensharponlinejudge.project.repository;

import org.channel.ensharponlinejudge.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
}
