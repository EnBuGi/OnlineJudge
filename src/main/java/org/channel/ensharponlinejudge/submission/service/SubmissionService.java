package org.channel.ensharponlinejudge.submission.service;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionCreatedEvent;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UUID submit(UUID userId, UUID projectId, String repoUrl) {

        List<SubmissionStatus> inProgress =
                List.of(SubmissionStatus.ENQUEUING, SubmissionStatus.QUEUED, SubmissionStatus.PROCESSING);

        Project project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(() -> new BusinessException(SubmissionErrorCode.PROJECT_NOT_FOUND));

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BusinessException(SubmissionErrorCode.USER_NOT_FOUND));

        if (submissionRepository.existsByUserIdAndProjectIdAndStatusIn(userId, projectId, inProgress)) {
            throw new BusinessException(SubmissionErrorCode.SUBMISSION_IN_PROGRESS);
        }

        Submission submission = Submission.initialize(userId, projectId, repoUrl);
        Submission savedSubmission = submissionRepository.save(submission);


        //내부 이벤트 발행
        applicationEventPublisher.publishEvent(new SubmissionCreatedEvent(savedSubmission.getId()));

        return savedSubmission.getId();
    }
}
