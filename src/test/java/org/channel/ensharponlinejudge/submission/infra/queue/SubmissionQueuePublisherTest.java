package org.channel.ensharponlinejudge.submission.infra.queue;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionJudgeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SubmissionQueuePublisherTest {

  @Test
  @DisplayName("직렬화 성공 시 Redis LIST rightPush가 호출된다")
  void Given직렬화에_성공_시Whenenqueue를_호출하면ExpectRedisListRightPush가_호출된다()
      throws JsonProcessingException {

    UUID submissionId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";
    String testCodeGetUrl = "https://example.com/test.zip";

    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    ListOperations<String, String> listOperations = mock(ListOperations.class);

    when(redisTemplate.opsForList()).thenReturn(listOperations);

    SubmissionJudgeRequest submissionJudgeRequest =
        new SubmissionJudgeRequest(
            submissionId,
            userId,
            projectId,
            repoUrl,
            testCodeGetUrl,
            1000,
            512,
            "JAVA",
            Collections.emptyList(),
            null);

    String json = "{\"fake\":\"json\"}";
    when(objectMapper.writeValueAsString(submissionJudgeRequest)).thenReturn(json);

    SubmissionQueuePublisher publisher = new SubmissionQueuePublisher(redisTemplate, objectMapper);

    publisher.enqueue(submissionJudgeRequest);

    verify(listOperations).rightPush(eq(SubmissionQueuePublisher.QUEUE_KEY), eq(json));
    verifyNoMoreInteractions(listOperations);
  }

  @Test
  @DisplayName("직렬화 실패(JsonProcessingException) 시 QUEUE_PUBLISH_FAILED BusinessException을 던진다")
  void Given직렬화실패_시Whenenqueue를_호출하면ExpoectQUEUE_PUBLISH_FAILED예외가_발생햐댜()
      throws JsonProcessingException {

    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);

    UUID submissionId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";
    String testCodeGetUrl = "https://example.com/test.zip";

    SubmissionJudgeRequest submissionJudgeRequest =
        new SubmissionJudgeRequest(
            submissionId,
            userId,
            projectId,
            repoUrl,
            testCodeGetUrl,
            1000,
            512,
            "JAVA",
            Collections.emptyList(),
            null);

    when(objectMapper.writeValueAsString(submissionJudgeRequest))
        .thenThrow(new JsonProcessingException("boom") {});
    SubmissionQueuePublisher publisher = new SubmissionQueuePublisher(redisTemplate, objectMapper);

    assertThatThrownBy(() -> publisher.enqueue(submissionJudgeRequest))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> {
              BusinessException be = (BusinessException) ex;
              org.assertj.core.api.Assertions.assertThat(be.getErrorCode())
                  .isEqualTo(SubmissionErrorCode.QUEUE_PUBLISH_FAILED);
            });

    verifyNoInteractions(redisTemplate);
  }
}
