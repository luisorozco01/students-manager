package com.jloroz.students.service;

import com.jloroz.students.dto.StudentRequest;
import com.jloroz.students.dto.StudentResponse;
import com.jloroz.students.entity.Student;
import com.jloroz.students.exceptions.StudentNotFoundException;
import com.jloroz.students.mapper.StudentMapper;
import com.jloroz.students.repository.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;
    private final StudentMapper mapper;

    public StudentService(StudentRepository repository, StudentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    public StudentResponse create(StudentRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getOrThrow(id);
        mapper.updateEntity(student, request);
        return mapper.toResponse(student);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Student getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    }
}
