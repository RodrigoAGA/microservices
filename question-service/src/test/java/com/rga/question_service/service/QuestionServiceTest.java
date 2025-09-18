package com.rga.question_service.service;

import com.rga.question_service.dao.QuestionDao;
import com.rga.question_service.model.Question;
import com.rga.question_service.model.QuestionWrapper;
import com.rga.question_service.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void getAllQuestionsReturnsOkWhenDaoSucceeds() {
        List<Question> questions = List.of(createQuestion(1));
        when(questionDao.findAll()).thenReturn(questions);

        ResponseEntity<List<Question>> response = questionService.getAllQuestions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactlyElementsOf(questions);
    }

    @Test
    void getAllQuestionsReturnsBadRequestWhenDaoThrows() {
        when(questionDao.findAll()).thenThrow(new RuntimeException("database down"));

        ResponseEntity<List<Question>> response = questionService.getAllQuestions();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getQuestionsByCategoryReturnsOkWhenDaoSucceeds() {
        List<Question> questions = List.of(createQuestion(5));
        when(questionDao.findByCategory("java")).thenReturn(questions);

        ResponseEntity<List<Question>> response = questionService.getQuestionsByCategory("java");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactlyElementsOf(questions);
    }

    @Test
    void getQuestionsByCategoryReturnsBadRequestWhenDaoThrows() {
        when(questionDao.findByCategory("java")).thenThrow(new RuntimeException("no category"));

        ResponseEntity<List<Question>> response = questionService.getQuestionsByCategory("java");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void addQuestionPersistsEntityAndReturnsSuccess() {
        Question question = createQuestion(10);

        ResponseEntity<String> response = questionService.addQuestion(question);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionDao).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(question);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("success", response.getBody());
    }

    @Test
    void getQuestionsForQuizReturnsIdsFromDao() {
        List<Integer> ids = List.of(1, 2, 3);
        when(questionDao.findRandomQuestionsByCategory("spring", 3)).thenReturn(ids);

        ResponseEntity<List<Integer>> response = questionService.getQuestionsForQuiz("spring", 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactlyElementsOf(ids);
    }

    @Test
    void getQuestionsFromIdReturnsWrappersForEachQuestion() {
        Question question = createQuestion(7);
        when(questionDao.findById(7)).thenReturn(Optional.of(question));

        ResponseEntity<List<QuestionWrapper>> response = questionService.getQuestionsFromId(List.of(7));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        QuestionWrapper wrapper = response.getBody().get(0);
        assertEquals(question.getId(), wrapper.getId());
        assertEquals(question.getQuestionTitle(), wrapper.getQuestionTitle());
        assertEquals(question.getOption1(), wrapper.getOption1());
        assertEquals(question.getOption2(), wrapper.getOption2());
        assertEquals(question.getOption3(), wrapper.getOption3());
        assertEquals(question.getOption4(), wrapper.getOption4());
    }

    @Test
    void getScoreCountsOnlyCorrectAnswers() {
        Question questionOne = createQuestion(1);
        questionOne.setRightAnswer("A");
        Question questionTwo = createQuestion(2);
        questionTwo.setRightAnswer("B");

        Response responseOne = new Response();
        responseOne.setId(1);
        responseOne.setResponse("A");
        Response responseTwo = new Response();
        responseTwo.setId(2);
        responseTwo.setResponse("C");

        when(questionDao.findById(1)).thenReturn(Optional.of(questionOne));
        when(questionDao.findById(2)).thenReturn(Optional.of(questionTwo));

        ResponseEntity<Integer> response = questionService.getScore(List.of(responseOne, responseTwo));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody());
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
}
