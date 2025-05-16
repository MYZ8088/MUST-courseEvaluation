package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByName(String name);
    List<Teacher> findByNameContaining(String name);
    List<Teacher> findByFacultyId(Long facultyId);
    boolean existsByName(String name);
} 