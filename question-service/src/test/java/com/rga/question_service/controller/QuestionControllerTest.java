package com.rga.question_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rga.question_service.model.Question;
import com.rga.question_service.model.QuestionWrapper;
import com.rga.question_service.model.Response;
import com.rga.question_service.service.QuestionService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionService questionService;

    @Test
    void getAllQuestionsReturnsListFromService() throws Exception {
        List<Question> questions = List.of(createQuestion(1));
        when(questionService.getAllQuestions()).thenReturn(new ResponseEntity<>(questions, HttpStatus.OK));

        mockMvc.perform(get("/question/allQuestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].questionTitle").value("Question 1"));

        verify(questionService).getAllQuestions();
    }

    @Test
    void getQuestionsByCategoryPassesPathVariableToService() throws Exception {
        List<Question> questions = List.of(createQuestion(2));
        when(questionService.getQuestionsByCategory("java")).thenReturn(new ResponseEntity<>(questions, HttpStatus.OK));

        mockMvc.perform(get("/question/category/{category}", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(questionService).getQuestionsByCategory("java");
    }

    @Test
    void addQuestionPassesBodyToService() throws Exception {
        Question question = createQuestion(3);
        when(questionService.addQuestion(any(Question.class))).thenReturn(new ResponseEntity<>("success", HttpStatus.CREATED));

        mockMvc.perform(post("/question/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andExpect(status().isCreated())
                .andExpect(content().string("success"));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionService).addQuestion(captor.capture());
        Question captured = captor.getValue();
        assertThat(captured.getQuestionTitle()).isEqualTo("Question 3");
        assertThat(captured.getCategory()).isEqualTo("general");
    }

    @Test
    void getQuestionsForQuizSendsQueryParamsToService() throws Exception {
        List<Integer> ids = List.of(4, 5);
        when(questionService.getQuestionsForQuiz("spring", 2)).thenReturn(new ResponseEntity<>(ids, HttpStatus.OK));

        mockMvc.perform(get("/question/generate")
                        .param("categoryName", "spring")
                        .param("numQuestions", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(4));

        verify(questionService).getQuestionsForQuiz("spring", 2);
    }

    @Test
    void getQuestionsFromIdSendsBodyToService() throws Exception {
        List<Integer> ids = List.of(6, 7);
        List<QuestionWrapper> wrappers = List.of(createWrapper(6));
        when(questionService.getQuestionsFromId(anyList())).thenReturn(new ResponseEntity<>(wrappers, HttpStatus.OK));

        mockMvc.perform(post("/question/getQuestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(6));

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
        verify(questionService).getQuestionsFromId(captor.capture());
        assertThat(captor.getValue()).containsExactlyElementsOf(ids);
    }

    @Test
    void getScoreSendsResponsesToService() throws Exception {
        List<Response> responses = List.of(createResponse(8, "A"));
        when(questionService.getScore(anyList())).thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        mockMvc.perform(post("/question/getScore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responses)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        ArgumentCaptor<List<Response>> captor = ArgumentCaptor.forClass(List.class);
        verify(questionService).getScore(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    private Question createQuestion(int id) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionTitle("Question " + id);
        question.setOption1("A");
        question.setOption2("B");
        question.setOption3("C");
        question.setOption4("D");
        question.setRightAnswer("A");
        question.setCategory("general");
        question.setDifficultylevel("easy");
        return question;
    }

    private QuestionWrapper createWrapper(int id) {
        QuestionWrapper wrapper = new QuestionWrapper();
        wrapper.setId(id);
        wrapper.setQuestionTitle("Question " + id);
        wrapper.setOption1("A");
        wrapper.setOption2("B");
        wrapper.setOption3("C");
        wrapper.setOption4("D");
        return wrapper;
    }

    private Response createResponse(int id, String answer) {
        Response response = new Response();
        response.setId(id);
        response.setResponse(answer);
        return response;
    }
}
