package org.channel.ensharponlinejudge.submission.presentation;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.channel.ensharponlinejudge.submission.presentation.dto.request.SubmitRequest;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = SubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubmissionControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockitoBean SubmissionService submissionService;

  @Test
  @DisplayName("repoUrl이 GitHub 형식이 아니면 400을 반환한다")
  void Given깃허브Url이_아닌데_When제출을_요청하면_Expect400에러가_발생한다() throws Exception {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    SubmitRequest submitRequest = new SubmitRequest("http://naver.com/test/ex");

    mockMvc
        .perform(
            post("/api/v1/projects/" + projectId + "/submissions")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(submitRequest)))
        .andExpect(status().isBadRequest());

    then(submissionService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("repoUrl이 올바르면 200을 반환한다")
  void Given_repoUrl이_올바른Url인데_When제출을_요청하면_Expect201을반환한다() throws Exception {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    SubmitRequest submitRequest = new SubmitRequest("https://github.com/vvineey/example");

    mockMvc
        .perform(
            post("/api/v1/projects/" + projectId + "/submissions")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitRequest)))
        .andExpect(status().isCreated());

    then(submissionService).should().submit(userId, projectId, submitRequest.repoUrl());
  }
}
