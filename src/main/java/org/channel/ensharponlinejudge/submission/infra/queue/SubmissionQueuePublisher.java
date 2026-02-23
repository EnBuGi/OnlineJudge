package org.channel.ensharponlinejudge.submission.infra.queue;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionJudgeRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionQueuePublisher {

    public static final String QUEUE_KEY = "oj:submission:queue";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void enqueue(SubmissionJudgeRequest submissionJudgeRequest) {

        try {
            String json = objectMapper.writeValueAsString(submissionJudgeRequest);
            stringRedisTemplate.opsForList().rightPush(QUEUE_KEY, json);
        }
        catch(JsonProcessingException e){
             throw new BusinessException(SubmissionErrorCode.QUEUE_PUBLISH_FAILED);
        }
    }
}
