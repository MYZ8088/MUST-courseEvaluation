package com.must.courseevaluation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 120, message = "密码长度必须在6到120个字符之间")
    private String password;
    
    @Email(message = "请提供有效的电子邮件地址")
    @NotBlank(message = "电子邮件不能为空")
    private String email;
    
    private String emailCode;
    
    private String studentId;
    
    private String fullName;
} 