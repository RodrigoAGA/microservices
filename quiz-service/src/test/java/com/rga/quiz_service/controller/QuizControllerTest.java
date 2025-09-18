package com.rga.quiz_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rga.quiz_service.model.QuestionWrapper;
import com.rga.quiz_service.model.QuizDto;
import com.rga.quiz_service.model.Response;
import com.rga.quiz_service.service.QuizService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizService quizService;

    @Test
    void createQuizForwardsDtoToService() throws Exception {
        QuizDto dto = new QuizDto();
        dto.setCategoryName("java");
        dto.setNumQuestions(2);
        dto.setTitle("Java Quiz");
        when(quizService.createQuiz(anyString(), anyInt(), anyString()))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.CREATED));

        mockMvc.perform(post("/quiz/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Success"));

        verify(quizService).createQuiz("java", 2, "Java Quiz");
    }

    @Test
    void getQuizQuestionsReturnsBodyFromService() throws Exception {
        List<QuestionWrapper> wrappers = List.of(new QuestionWrapper(1, "Question 1", "A", "B", "C", "D"));
        when(quizService.getQuizQuestions(5)).thenReturn(new ResponseEntity<>(wrappers, HttpStatus.OK));

        mockMvc.perform(get("/quiz/get/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(quizService).getQuizQuestions(5);
    }

    @Test
    void submitQuizPassesResponsesToService() throws Exception {
        List<Response> responses = List.of(createResponse(1, "A"));
        when(quizService.calculateResult(anyInt(), anyList()))
                .thenReturn(new ResponseEntity<>(2, HttpStatus.OK));

        mockMvc.perform(post("/quiz/submit/{id}", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responses)))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        ArgumentCaptor<List<Response>> captor = ArgumentCaptor.forClass(List.class);
        verify(quizService).calculateResult(9, captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    private Response createResponse(int id, String answer) {
        Response response = new Response();
        response.setId(id);
        response.setResponse(answer);
        return response;
    }
}
