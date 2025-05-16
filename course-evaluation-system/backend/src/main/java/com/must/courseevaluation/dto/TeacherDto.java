package com.must.courseevaluation.dto;

import com.must.courseevaluation.model.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {
    
    private Long id;
    
    @NotBlank(message = "教师姓名不能为空")
    @Size(max = 100, message = "教师姓名不能超过100个字符")
    private String name;
    
    private String title;
    
    @Email(message = "请提供有效的电子邮件地址")
    private String email;
    
    @Size(max = 1000, message = "个人简介不能超过1000个字符")
    private String bio;
    
    @NotNull(message = "院系ID不能为空")
    private Long facultyId;
    
    private String facultyName;
    
    public static TeacherDto fromEntity(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        
        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setTitle(teacher.getTitle());
        dto.setEmail(teacher.getEmail());
        dto.setBio(teacher.getBio());
        if (teacher.getFaculty() != null) {
            dto.setFacultyId(teacher.getFaculty().getId());
            dto.setFacultyName(teacher.getFaculty().getName());
        }
        return dto;
    }
} 