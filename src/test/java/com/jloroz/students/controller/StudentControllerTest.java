package com.jloroz.students.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jloroz.students.dto.StudentRequest;
import com.jloroz.students.dto.StudentResponse;
import com.jloroz.students.repository.StudentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository repository;

    private RestClient client;

    @BeforeEach
    void setUp() {
        // The server runs on a separate thread, so test-managed rollback is not possible; clean explicitly.
        repository.deleteAll();
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/students")
                .defaultStatusHandler(status -> true, (req, res) -> { })
                .build();
    }

    private ResponseEntity<StudentResponse> create(String name, int age, String className) {
        return client.post().body(new StudentRequest(name, age, className))
                .retrieve().toEntity(StudentResponse.class);
    }

    @Test
    @DisplayName("Should create a student and return 201")
    void shouldCreateStudent() {
        ResponseEntity<StudentResponse> response = create("Ana", 15, "10A");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Ana");
        assertThat(response.getBody().className()).isEqualTo("10A");
    }

    @Test
    @DisplayName("Should return all students")
    void shouldReturnAllStudents() {
        create("Ana", 15, "10A");
        create("Luis", 16, "11B");

        ResponseEntity<List<StudentResponse>> response = client.get().retrieve()
                .toEntity(new ParameterizedTypeReference<>() { });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(StudentResponse::name).containsExactlyInAnyOrder("Ana", "Luis");
    }

    @Test
    @DisplayName("Should return a student by id")
    void shouldReturnStudentById() {
        Long id = create("Ana", 15, "10A").getBody().id();

        ResponseEntity<StudentResponse> response = client.get().uri("/{id}", id)
                .retrieve().toEntity(StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().age()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should update an existing student")
    void shouldUpdateStudent() {
        Long id = create("Ana", 15, "10A").getBody().id();

        ResponseEntity<StudentResponse> response = client.put().uri("/{id}", id)
                .body(new StudentRequest("Ana Maria", 16, "11A"))
                .retrieve().toEntity(StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new StudentResponse(id, "Ana Maria", 16, "11A"));
    }

    @Test
    @DisplayName("Should delete a student and return 204")
    void shouldDeleteStudent() {
        Long id = create("Ana", 15, "10A").getBody().id();

        ResponseEntity<Void> deleted = client.delete().uri("/{id}", id).retrieve().toBodilessEntity();
        ResponseEntity<ProblemDetail> afterwards = client.get().uri("/{id}", id)
                .retrieve().toEntity(ProblemDetail.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(afterwards.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when student does not exist")
    void shouldReturn404WhenNotFound() {
        ResponseEntity<ProblemDetail> response = client.get().uri("/{id}", 9999)
                .retrieve().toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when request is invalid")
    void shouldReturn400WhenInvalid() {
        ResponseEntity<ProblemDetail> response = client.post()
                .body(new StudentRequest(" ", 0, ""))
                .retrieve().toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getProperties()).containsKey("errors");
    }
}
