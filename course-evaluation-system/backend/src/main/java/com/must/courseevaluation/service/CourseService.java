package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.model.Course;

import java.util.List;

public interface CourseService {
    List<CourseDto> getAllCourses();
    CourseDto getCourseById(Long id);
    List<CourseDto> getCoursesByFaculty(Long facultyId);
    List<CourseDto> getCoursesByTeacher(Long teacherId);
    List<CourseDto> getCoursesByType(Course.CourseType type);
    List<CourseDto> searchCourses(String keyword);
    CourseDto createCourse(CourseDto courseDto);
    CourseDto updateCourse(CourseDto courseDto);
    void deleteCourse(Long id);
} 