package org.channel.ensharponlinejudge.project.repository;

import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {}
