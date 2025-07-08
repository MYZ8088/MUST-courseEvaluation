package com.must.courseevaluation.controller;

import com.must.courseevaluation.security.InputValidationUtils;
import com.must.courseevaluation.service.CacheService;
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

    @Autowired
    private InputValidationUtils inputValidationUtils;

    @Autowired
    private CacheService cacheService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        // 尝试从缓存获取
        Object cachedCourses = cacheService.getCachedFaculties(); // 这里应该是课程缓存
        if (cachedCourses != null) {
            return ResponseEntity.ok((List<CourseDto>) cachedCourses);
        }
        
        List<CourseDto> courses = courseService.getAllCourses();
        
        // 缓存结果
        cacheService.cacheFaculties(courses); // 这里应该是课程缓存
        
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        // 尝试从缓存获取
        Object cachedCourse = cacheService.getCachedCourse(id);
        if (cachedCourse != null) {
            return ResponseEntity.ok((CourseDto) cachedCourse);
        }
        
        CourseDto course = courseService.getCourseById(id);
        
        // 缓存结果
        cacheService.cacheCourse(id, course);
        
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
        // 输入验证
        if (inputValidationUtils.containsSqlInjection(keyword) || 
            inputValidationUtils.containsXss(keyword)) {
            return ResponseEntity.badRequest().build();
        }
        
        // 清理输入
        String cleanKeyword = inputValidationUtils.sanitizeInput(keyword);
        
        List<CourseDto> courses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto);
       
       // 清除相关缓存
       cacheService.evictFacultiesCache();
       
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        courseDto.setId(id);
        CourseDto updatedCourse = courseService.updateCourse(courseDto);
        
        // 清除相关缓存
        cacheService.evictCourseCache(id);
        cacheService.evictFacultiesCache();
        
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        
        // 清除相关缓存
        cacheService.evictCourseCache(id);
        cacheService.evictFacultiesCache();
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<Map<String, Object>> getCourseRatings(@PathVariable Long id) {
        // 尝试从缓存获取评分统计
        Object cachedRating = cacheService.getCachedRating("course", id);
        if (cachedRating != null) {
            return ResponseEntity.ok((Map<String, Object>) cachedRating);
        }
        
        Map<String, Object> ratings = reviewService.getCourseRatings(id);
        
        // 缓存评分统计
        cacheService.cacheRating("course", id, ratings);
        
        return ResponseEntity.ok(ratings);
    }
} 