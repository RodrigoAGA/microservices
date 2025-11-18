package com.rga.question_service;

import com.rga.question_service.dao.QuestionDao;
import com.rga.question_service.model.Question;
import com.rga.question_service.model.Response;
import com.rga.question_service.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional; // ¡Importante para Mockito!

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTests {

    @InjectMocks
    private QuestionService questionService;

    @Mock
    private QuestionDao questionDao;

    @Test
    void testCalculateScore_CorrectAnswers() {
        // 1. Datos de prueba: El usuario responde 'Respuesta A' y 'Respuesta B'.
        List<Response> responses = Arrays.asList(
            new Response(1, "Respuesta A"), // Usuario responde A
            new Response(2, "Respuesta B")  // Usuario responde B
        );

        // 2. Preguntas que están en la base de datos (con las respuestas correctas).
        // (Asumimos que Question tiene 9 argumentos en el constructor con @AllArgsConstructor)
        Question q1 = new Question(1, "Texto 1",  "A", "B", "C", "D", "Respuesta A", "Java", "Easy");
        Question q2 = new Question(2, "Texto 2", "A", "B", "C", "D", "Respuesta B", "Java", "Easy");
        
        // 3. Simulación (Mocking) de las llamadas al DAO (¡CORRECCIÓN CLAVE 1!)
        // El servicio llama findById(id) en un bucle, no findAllById.
        // Simulamos que el DAO devuelve un Optional con la pregunta.

        // Cuando el servicio pide el ID 1, devuelve Optional.of(q1)
        when(questionDao.findById(1)).thenReturn(Optional.of(q1));
        // Cuando el servicio pide el ID 2, devuelve Optional.of(q2)
        when(questionDao.findById(2)).thenReturn(Optional.of(q2));

        // 4. Ejecutar el método a probar
        ResponseEntity<Integer> result = questionService.getScore(responses);

        // 5. Verificar el resultado (¡CORRECCIÓN CLAVE 2 y 3!)

        // Verificamos el estado HTTP (debe ser 200 OK)
        assertEquals(HttpStatus.OK, result.getStatusCode(), "El estado HTTP debe ser 200 OK.");
        
        // Verificamos el cuerpo del resultado (el score numérico)
        // El puntaje esperado es 2 porque ambas respuestas del usuario (A y B) coinciden con rightAnswer de q1 y q2.
        assertEquals(2, result.getBody().intValue(), "El puntaje debería ser 2 (ambas correctas)");
    }
}