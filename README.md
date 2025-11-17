# Plataforma de Quiz basada en Microservicios

Este repositorio contiene un sistema distribuido construido con Spring Boot cuya finalidad es administrar preguntas y cuestionarios utilizando una arquitectura de microservicios. El ecosistema está formado por un **Service Registry (Eureka)**, un **API Gateway (Spring Cloud Gateway)** y dos microservicios de dominio: **question-service** y **quiz-service**. Cada servicio se empaqueta con Maven y utiliza PostgreSQL como base de datos.

## Arquitectura general

```
[Clientes]
    │
    ▼
API Gateway (8765)
    │           ┌─────────────────────────────┐
    ├──────────▶│ question-service (JPA + DB)  │
    │           └─────────────────────────────┘
    │           ┌─────────────────────────────┐
    └──────────▶│   quiz-service (Feign/JPA)   │
                └─────────────────────────────┘
                        │
                        ▼
               Service Registry (8761)
```

* **service-registry** expone un servidor Eureka para que los microservicios se registren dinámicamente.
* **api-gateway** enruta las peticiones entrantes hacia los microservicios registrados, habilitando descubrimiento dinámico (`spring.cloud.gateway.discovery.locator.enabled=true`).
* **question-service** encapsula toda la gestión de preguntas y ofrece endpoints REST para consultar, crear y evaluar respuestas.
* **quiz-service** se encarga de generar cuestionarios apoyándose en Feign (`@EnableFeignClients`) para consumir los endpoints del question-service.

## Requisitos previos

1. **Java 17** (o la versión configurada en los `pom.xml`).
2. **Maven 3.9+** para compilar y ejecutar.
3. **PostgreSQL** con dos bases de datos accesibles localmente:
   * `questiondb` para `question-service`.
   * `quizdb` para `quiz-service`.
4. Variables de conexión configuradas según `application.properties` (usuario `postgres`, contraseña `0000` por defecto). Ajuste estos valores si su entorno difiere.

## Puertos y servicios

| Servicio            | Puerto | Descripción |
|---------------------|--------|-------------|
| service-registry    | 8761   | Servidor Eureka para registro/descubrimiento. |
| api-gateway         | 8765   | Puerta de enlace reactiva que enruta a los microservicios registrados. |
| question-service    | 8080*  | API de preguntas (puerto definido por Spring, puede variar si se configura). |
| quiz-service        | 8090   | API de cuestionarios, consume question-service vía Feign. |

\*`question-service` no fija un puerto en `application.properties`, por lo que utilizará el puerto por defecto (8080) salvo que se defina otra propiedad.

## Configuración de cada microservicio

### service-registry

* Arranca con `@EnableEurekaServer` y no se registra a sí mismo (`eureka.client.fetch-registry=false`, `register-with-eureka=false`).
* Debe ser el primer servicio en levantarse para que los demás puedan registrarse correctamente.

### api-gateway

* Habilita el `DiscoveryClientRouteDefinitionLocator`, lo que genera rutas automáticamente para cada servicio registrado en Eureka utilizando el patrón `http://<host>:8765/<service-id>/**`.
* Ideal para centralizar políticas de seguridad, rate limiting o métricas.

### question-service

* Utiliza Spring Data JPA (`QuestionDao`) para persistir la entidad `Question` en PostgreSQL.
* `QuestionService` publica operaciones REST para:
  - Listar todas las preguntas.
  - Filtrar por categoría.
  - Crear nuevas preguntas.
  - Seleccionar preguntas aleatorias por categoría para generar quizzes (`findRandomQuestionsByCategory`).
  - Convertir preguntas a `QuestionWrapper` para evitar exponer la respuesta correcta.
  - Calcular la puntuación en base a respuestas enviadas.

### quiz-service

* Define `Quiz` como entidad que almacena un título y la lista de IDs de preguntas generadas.
* El `QuizService` se comunica con question-service usando `QuizInterface` (Feign client) para:
  - Solicitar IDs aleatorios al crear un quiz.
  - Recuperar el detalle de las preguntas al resolver un quiz.
  - Calcular el puntaje enviando las respuestas al question-service.

## Endpoints REST

### question-service (`/question`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET    | `/allQuestions` | Retorna todas las preguntas almacenadas. |
| GET    | `/category/{category}` | Obtiene preguntas de una categoría específica. |
| POST   | `/add` | Crea una nueva pregunta (body: `Question`). |
| GET    | `/generate?categoryName=...&numQuestions=...` | Devuelve una lista de IDs aleatorios para armar un quiz. |
| POST   | `/getQuestions` | Recibe una lista de IDs y retorna `QuestionWrapper` sin las respuestas correctas. |
| POST   | `/getScore` | Calcula y devuelve el número de respuestas correctas. |

### quiz-service (`/quiz`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST   | `/create` | Crea un nuevo quiz a partir de una categoría, cantidad de preguntas y título (body: `QuizDto`). |
| GET    | `/get/{id}` | Devuelve la lista de preguntas asociadas al quiz con ID indicado. |
| POST   | `/submit/{id}` | Envía las respuestas del quiz y retorna el puntaje total. |

## Orden de arranque recomendado

1. `service-registry`
2. `question-service`
3. `quiz-service`
4. `api-gateway`

Es importante que `question-service` esté arriba antes de crear/consultar quizzes, ya que `quiz-service` depende de sus endpoints.

## Comandos útiles

Desde la raíz del repositorio puede compilar cada módulo con Maven Wrapper:

```bash
cd service-registry && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
cd question-service && ./mvnw spring-boot:run
cd quiz-service && ./mvnw spring-boot:run
```

> En Windows utilice `mvnw.cmd`.

Para ejecutar pruebas unitarias (si existieran), use `./mvnw test` dentro de cada módulo.

## Variables y ajustes

* Modifique `spring.datasource.*` en cada `application.properties` para apuntar al host, puerto, base de datos y credenciales correctas.
* Ajuste `server.port` si necesita evitar conflictos de puertos.
* Puede agregar configuración adicional de Eureka (zona, tiempos de latido) según el entorno de despliegue.

## Futuras mejoras sugeridas

* Implementar seguridad (OAuth2/JWT) y rate limiting en el API Gateway.
* Agregar pruebas unitarias y de integración para servicios críticos.
* Incluir scripts de migración (Flyway/Liquibase) y contenedores Docker para simplificar la orquestación.
* Exponer métricas y tracing (Micrometer + Zipkin) para observabilidad.

---

Con este README dispondrá de una referencia rápida para comprender, configurar y ejecutar la plataforma de cuestionarios basada en microservicios.
