package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.CourseDto;
import com.must.courseevaluation.exception.ResourceNotFoundException;
import com.must.courseevaluation.model.Course;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.repository.CourseRepository;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.repository.TeacherRepository;
import com.must.courseevaluation.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + id));
        return CourseDto.fromEntity(course);
    }

    @Override
    public List<CourseDto> getCoursesByFaculty(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在，ID: " + facultyId));
        return courseRepository.findByFaculty(faculty).stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getCoursesByTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("教师不存在，ID: " + teacherId));
        return courseRepository.findByTeacher(teacher).stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getCoursesByType(Course.CourseType type) {
        return courseRepository.findByType(type).stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> searchCourses(String keyword) {
        return courseRepository.findByNameContaining(keyword).stream()
                .map(CourseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        // 检查课程代码是否已存在
        if (courseRepository.existsByCode(courseDto.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + courseDto.getCode());
        }

        // 获取院系
        Faculty faculty = facultyRepository.findById(courseDto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在，ID: " + courseDto.getFacultyId()));

        // 构建课程实体
        Course course = new Course();
        course.setCode(courseDto.getCode());
        course.setName(courseDto.getName());
        course.setCredits(courseDto.getCredits());
        course.setDescription(courseDto.getDescription());
        course.setType(Course.CourseType.valueOf(courseDto.getType()));
        course.setAssessmentCriteria(courseDto.getAssessmentCriteria());
        course.setFaculty(faculty);

        // 如果指定了教师，设置教师
        if (courseDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("教师不存在，ID: " + courseDto.getTeacherId()));
            course.setTeacher(teacher);
        }

        // 保存课程
        Course savedCourse = courseRepository.save(course);
        return CourseDto.fromEntity(savedCourse);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(CourseDto courseDto) {
        // 检查课程是否存在
        Course existingCourse = courseRepository.findById(courseDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseDto.getId()));

        // 如果更改了课程代码，检查新代码是否已存在
        if (!existingCourse.getCode().equals(courseDto.getCode()) && courseRepository.existsByCode(courseDto.getCode())) {
            throw new IllegalArgumentException("课程代码已存在: " + courseDto.getCode());
        }

        // 获取院系
        Faculty faculty = facultyRepository.findById(courseDto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("院系不存在，ID: " + courseDto.getFacultyId()));

        // 更新课程信息
        existingCourse.setCode(courseDto.getCode());
        existingCourse.setName(courseDto.getName());
        existingCourse.setCredits(courseDto.getCredits());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setType(Course.CourseType.valueOf(courseDto.getType()));
        existingCourse.setAssessmentCriteria(courseDto.getAssessmentCriteria());
        existingCourse.setFaculty(faculty);

        // 更新教师信息
        if (courseDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(courseDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("教师不存在，ID: " + courseDto.getTeacherId()));
            existingCourse.setTeacher(teacher);
        } else {
            existingCourse.setTeacher(null);
        }

        // 保存更新后的课程
        Course updatedCourse = courseRepository.save(existingCourse);
        return CourseDto.fromEntity(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("课程不存在，ID: " + id);
        }
        courseRepository.deleteById(id);
    }
} 