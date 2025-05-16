package com.must.courseevaluation.controller;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.service.CourseService;
import com.must.courseevaluation.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<CourseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<CourseDto>> getCoursesByFaculty(@PathVariable Long facultyId) {
        List<CourseDto> courses = courseService.getCoursesByFaculty(facultyId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseDto>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<CourseDto> courses = courseService.getCoursesByTeacher(teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CourseDto>> getCoursesByType(@PathVariable String type) {
        try {
            Course.CourseType courseType = Course.CourseType.valueOf(type);
            List<CourseDto> courses = courseService.getCoursesByType(courseType);
            return ResponseEntity.ok(courses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDto>> searchCourses(@RequestParam String keyword) {
        List<CourseDto> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        courseDto.setId(id);
        CourseDto updatedCourse = courseService.updateCourse(courseDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<Map<String, Object>> getCourseRatings(@PathVariable Long id) {
        Map<String, Object> ratings = reviewService.getCourseRatings(id);
        return ResponseEntity.ok(ratings);
    }
} 