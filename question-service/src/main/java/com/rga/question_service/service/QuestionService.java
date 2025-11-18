package com.rga.question_service.service;

import com.rga.question_service.model.Question;
import com.rga.question_service.model.QuestionWrapper;
import com.rga.question_service.model.Response;
import com.rga.question_service.dao.QuestionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Importar Optional

@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDao;

    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            return new ResponseEntity<>(questionDao.findAll(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {
        try {
            return new ResponseEntity<>(questionDao.findByCategory(category),HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> addQuestion(Question question) {
        questionDao.save(question);
        return new ResponseEntity<>("success",HttpStatus.CREATED);
    }

    public ResponseEntity<List<Integer>> getQuestionsForQuiz(String categoryName, Integer numQuestions) {
        List<Integer> questions = questionDao.findRandomQuestionsByCategory(categoryName, numQuestions);
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    /**
     * MEJORA CRÍTICA: Se corrige el uso de .get() por .orElse(null) para seguridad.
     * Si un ID no existe, simplemente no se añade a la lista de preguntas.
     */
    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(List<Integer> questionIds) {
        List<QuestionWrapper> wrappers = new ArrayList<>();
        List<Question> questions = new ArrayList<>();
        
        for(Integer id: questionIds){
            Optional<Question> optionalQuestion = questionDao.findById(id);
            if (optionalQuestion.isPresent()) {
                questions.add(optionalQuestion.get());
            }
            // Si optionalQuestion está vacío, no se hace nada, evitando el error.
        }

        for(Question question: questions){
            QuestionWrapper wrapper = new QuestionWrapper();
            wrapper.setId(question.getId());
            wrapper.setQuestionTitle(question.getQuestionTitle());
            wrapper.setOption1(question.getOption1());
            wrapper.setOption2(question.getOption2());
            wrapper.setOption3(question.getOption3());
            wrapper.setOption4(question.getOption4());
            wrappers.add(wrapper);
        }

        return new ResponseEntity<>(wrappers, HttpStatus.OK);
    }

    /**
     * MEJORA CRÍTICA: Se corrige el uso de .get() por el manejo de Optional.
     * Si el ID de una respuesta no corresponde a una pregunta existente, se ignora (no se suma).
     */
    public ResponseEntity<Integer> getScore(List<Response> responses) {
        
        int right = 0;
        
        for(Response response : responses){
            // 1. Obtiene el Optional
            Optional<Question> optionalQuestion = questionDao.findById(response.getId());
            
            // 2. Verifica si el valor existe antes de intentar usarlo
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get(); // Es seguro usar .get() aquí
                
                if(response.getResponse().equals(question.getRightAnswer())) {
                    right++;
                }
            }
            // Si la pregunta no existe, el bucle simplemente continúa (right no se incrementa).
        }
        return new ResponseEntity<>(right, HttpStatus.OK);
    }
}