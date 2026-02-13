package org.channel.ensharponlinejudge.submission.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;


    @ManyToOne(fetch =FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "repo_url", length = 255, nullable = false)
    private String repoUrl;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column
    private int score;

    @CreationTimestamp
    @Column(name = "submit_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "time_usage")
    private int timeUsage;

    @Column(name = "memory_usage")
    private int memoryUsage;

    @Builder
    private Submission(User user, Project project, String repoUrl){
        this.user = user;
        this.project = project;
        this.repoUrl = repoUrl;
        this.status = SubmissionStatus.QUEUED;
    }

    public static Submission initialize(User user,
                                        Project project,
                                        String repoUrl){

        return Submission.builder()
                .user(user)
                .project(project)
                .repoUrl(repoUrl)
                .build();
    }

}
