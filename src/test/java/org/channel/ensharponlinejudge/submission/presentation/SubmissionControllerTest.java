package org.channel.ensharponlinejudge.submission.presentation;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.channel.ensharponlinejudge.submission.presentation.dto.request.SubmitRequest;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = SubmissionController.class)
public class SubmissionControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockitoBean SubmissionService submissionService;

  @Test
  void repoUrl이_깃허브_url이_아니면_400() throws Exception {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    SubmitRequest submitRequest = new SubmitRequest(userId, projectId, "http://naver.com/test/ex");

    mockMvc
        .perform(
            post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(submitRequest)))
        .andExpect(status().isBadRequest());

    then(submissionService).shouldHaveNoInteractions();
  }
}
