package com.must.courseevaluation.service.impl;

import com.must.courseevaluation.dto.TeacherDto;
import com.must.courseevaluation.model.Faculty;
import com.must.courseevaluation.model.Teacher;
import com.must.courseevaluation.repository.FacultyRepository;
import com.must.courseevaluation.repository.TeacherRepository;
import com.must.courseevaluation.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository, FacultyRepository facultyRepository) {
        this.teacherRepository = teacherRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public TeacherDto create(TeacherDto teacherDto) {
        // 检查教师名称是否已存在
        if (teacherRepository.existsByName(teacherDto.getName())) {
            throw new RuntimeException("教师名称已存在");
        }

        // 获取院系
        Faculty faculty = facultyRepository.findById(teacherDto.getFacultyId())
                .orElseThrow(() -> new RuntimeException("未找到指定的院系"));

        // 创建新教师
        Teacher teacher = new Teacher();
        teacher.setName(teacherDto.getName());
        teacher.setTitle(teacherDto.getTitle());
        teacher.setEmail(teacherDto.getEmail());
        teacher.setBio(teacherDto.getBio());
        teacher.setFaculty(faculty);

        // 保存教师
        Teacher savedTeacher = teacherRepository.save(teacher);

        // 返回DTO
        return TeacherDto.fromEntity(savedTeacher);
    }

    @Override
    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到教师，ID: " + id));
    }

    @Override
    public List<TeacherDto> findAll() {
        List<Teacher> teachers = teacherRepository.findAll();
        // 添加日志打印查询结果
        if (teachers.isEmpty()) {
            System.out.println("警告: 未找到任何教师数据");
        } else {
            System.out.println("成功查询到 " + teachers.size() + " 条教师数据");
            teachers.forEach(teacher -> {
                System.out.println("教师ID: " + teacher.getId() + ", 姓名: " + teacher.getName());
            });
        }
        return teachers.stream()
                .map(TeacherDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDto> findByFacultyId(Long facultyId) {
        return teacherRepository.findByFacultyId(facultyId).stream()
                .map(TeacherDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherDto update(Long id, TeacherDto teacherDto) {
        Teacher teacher = findById(id);

        // 如果名称已更改，检查是否与现有名称冲突
        if (teacherDto.getName() != null && !teacher.getName().equals(teacherDto.getName())) {
            if (teacherRepository.existsByName(teacherDto.getName())) {
                throw new RuntimeException("教师名称已存在");
            }
            teacher.setName(teacherDto.getName());
        }

        // 更新其他字段
        if (teacherDto.getTitle() != null) {
            teacher.setTitle(teacherDto.getTitle());
        }
        
        if (teacherDto.getEmail() != null) {
            teacher.setEmail(teacherDto.getEmail());
        }
        
        if (teacherDto.getBio() != null) {
            teacher.setBio(teacherDto.getBio());
        }

        // 更新院系
        if (teacherDto.getFacultyId() != null && (teacher.getFaculty() == null || !teacher.getFaculty().getId().equals(teacherDto.getFacultyId()))) {
            Faculty faculty = facultyRepository.findById(teacherDto.getFacultyId())
                    .orElseThrow(() -> new RuntimeException("未找到指定的院系"));
            teacher.setFaculty(faculty);
        }

        // 保存更新
        Teacher updatedTeacher = teacherRepository.save(teacher);

        // 返回DTO
        return TeacherDto.fromEntity(updatedTeacher);
    }

    @Override
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new RuntimeException("未找到教师，ID: " + id);
        }
        teacherRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return teacherRepository.existsByName(name);
    }
} 