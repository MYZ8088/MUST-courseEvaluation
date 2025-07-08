package com.must.courseevaluation.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class InputValidationUtils {
    
    // SQL注入检测模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror)",
        Pattern.CASE_INSENSITIVE
    );
    
    // XSS攻击检测模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)<script[^>]*>.*?</script>|javascript:|vbscript:|onload=|onerror=|onclick=",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 验证输入是否包含SQL注入攻击
     */
    public boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }
    
    /**
     * 验证输入是否包含XSS攻击
     */
    public boolean containsXss(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }
    
    /**
     * 清理输入，移除潜在的恶意内容
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // 移除HTML标签
        String cleaned = input.replaceAll("<[^>]+>", "");
        
        // 转义特殊字符
        cleaned = cleaned.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&#x27;")
                        .replace("/", "&#x2F;");
        
        return cleaned.trim();
    }
    
    /**
     * 验证邮箱格式
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }
    
    /**
     * 验证用户名格式
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        // 用户名只能包含字母、数字、下划线，长度3-20
        String usernameRegex = "^[a-zA-Z0-9_]{3,20}$";
        return Pattern.matches(usernameRegex, username);
    }
    
    /**
     * 验证密码强度
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 32) {
            return false;
        }
        
        // 必须包含大小写字母、数字
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLower && hasUpper && hasDigit;
    }
}