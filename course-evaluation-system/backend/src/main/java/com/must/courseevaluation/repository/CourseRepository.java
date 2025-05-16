package com.must.courseevaluation.repository;

import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    List<Course> findByNameContaining(String name);
    List<Course> findByFaculty(Faculty faculty);
    List<Course> findByTeacher(Teacher teacher);
    List<Course> findByType(Course.CourseType type);
    boolean existsByCode(String code);
} 