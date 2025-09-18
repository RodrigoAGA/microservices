package com.rga.quiz_service.service;

import com.rga.quiz_service.dao.QuizDao;
import com.rga.quiz_service.feign.QuizInterface;
import com.rga.quiz_service.model.QuestionWrapper;
import com.rga.quiz_service.model.Quiz;
import com.rga.quiz_service.model.Response;
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
class QuizServiceTest {

    @Mock
    private QuizDao quizDao;

    @Mock
    private QuizInterface quizInterface;

    @InjectMocks
    private QuizService quizService;

    @Test
    void createQuizStoresIdsReturnedByQuestionService() {
        List<Integer> ids = List.of(1, 2, 3);
        when(quizInterface.getQuestionsForQuiz("java", 3))
                .thenReturn(new ResponseEntity<>(ids, HttpStatus.OK));

        ResponseEntity<String> response = quizService.createQuiz("java", 3, "Java Quiz");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Success", response.getBody());

        ArgumentCaptor<Quiz> captor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizDao).save(captor.capture());
        Quiz saved = captor.getValue();
        assertEquals("Java Quiz", saved.getTitle());
        assertThat(saved.getQuestionsIds()).containsExactlyElementsOf(ids);
    }

    @Test
    void getQuizQuestionsReturnsWrappersFromFeignClient() {
        Quiz quiz = new Quiz();
        quiz.setId(5);
        quiz.setTitle("Spring");
        List<Integer> ids = List.of(10, 11);
        quiz.setQuestionsIds(ids);
        when(quizDao.findById(5)).thenReturn(Optional.of(quiz));

        List<QuestionWrapper> wrappers = List.of(createWrapper(10));
        ResponseEntity<List<QuestionWrapper>> wrapperResponse = new ResponseEntity<>(wrappers, HttpStatus.OK);
        when(quizInterface.getQuestionsFromId(ids)).thenReturn(wrapperResponse);

        ResponseEntity<List<QuestionWrapper>> response = quizService.getQuizQuestions(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsExactlyElementsOf(wrappers);
        verify(quizInterface).getQuestionsFromId(ids);
    }

    @Test
    void calculateResultDelegatesToFeignClient() {
        List<Response> responses = List.of(createResponse(1, "A"));
        ResponseEntity<Integer> scoreResponse = new ResponseEntity<>(1, HttpStatus.OK);
        when(quizInterface.getScore(responses)).thenReturn(scoreResponse);

        ResponseEntity<Integer> response = quizService.calculateResult(9, responses);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody());
        verify(quizInterface).getScore(responses);
    }

    private QuestionWrapper createWrapper(int id) {
        return new QuestionWrapper(id, "Question " + id, "A", "B", "C", "D");
    }

    private Response createResponse(int id, String answer) {
        Response response = new Response();
        response.setId(id);
        response.setResponse(answer);
        return response;
    }
}
