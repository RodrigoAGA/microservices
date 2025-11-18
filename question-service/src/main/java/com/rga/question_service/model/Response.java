package com.rga.question_service.model;

import lombok.Data;
import lombok.AllArgsConstructor; // 💡 ¡Añadir esta!
import lombok.NoArgsConstructor;   // 💡 ¡Añadir esta si la necesitas!

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private Integer id;
    private String response;
}
