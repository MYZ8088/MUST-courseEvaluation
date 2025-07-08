package com.must.courseevaluation.controller;

import com.must.courseevaluation.security.InputValidationUtils;
import com.must.courseevaluation.service.AsyncEmailService;
import com.must.courseevaluation.dto.UserDto;
import com.must.courseevaluation.dto.auth.JwtResponse;
import com.must.courseevaluation.dto.auth.LoginRequest;
import com.must.courseevaluation.dto.auth.RegisterRequest;
import com.must.courseevaluation.model.User;
import com.must.courseevaluation.security.UserDetailsImpl;
import com.must.courseevaluation.security.jwt.JwtUtils;
import com.must.courseevaluation.service.UserService;
import com.must.courseevaluation.service.VerificationCodeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private InputValidationUtils inputValidationUtils;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private AsyncEmailService asyncEmailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("尝试登录用户: {}", loginRequest.getUsername());
        
        // 输入验证
        if (inputValidationUtils.containsSqlInjection(loginRequest.getUsername()) ||
            inputValidationUtils.containsXss(loginRequest.getUsername())) {
            logger.warn("检测到恶意登录尝试: {}", loginRequest.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("message", "输入包含非法字符");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 先检查用户是否存在及状态
            try {
                User user = userService.findByUsername(loginRequest.getUsername());
                if (!user.isActive()) {
                    logger.warn("用户尝试登录但账户已被停用: {}", loginRequest.getUsername());
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "账户已被停用，请联系管理员");
                    return ResponseEntity.status(403).body(response);
                }
            } catch (UsernameNotFoundException e) {
                // 用户不存在的情况下，继续走正常认证流程，会抛出相应的认证异常
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User.Role role = User.Role.valueOf(userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .findFirst().orElse(User.Role.ROLE_STUDENT.name()));
            
            logger.info("用户登录成功: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), 
                    userDetails.getEmail(), role));
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", " " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("尝试注册新用户: {}", registerRequest.getUsername());
        
        // 输入验证
        if (!inputValidationUtils.isValidUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名格式不正确，只能包含字母、数字、下划线，长度3-20位"));
        }
        
        if (!inputValidationUtils.isValidEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "邮箱格式不正确"));
        }
        
        if (!inputValidationUtils.isStrongPassword(registerRequest.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码强度不够，必须包含大小写字母和数字，长度8-32位"));
        }
        
        // 清理输入
        registerRequest.setUsername(inputValidationUtils.sanitizeInput(registerRequest.getUsername()));
        registerRequest.setEmail(inputValidationUtils.sanitizeInput(registerRequest.getEmail()));
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 用户名已被使用!"));
        }

        // 检查电子邮件是否已存在
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 电子邮件已被使用!"));
        }

        // 首先检查是否是管理员特殊标识
        boolean isAdminCreate = "ADMIN_CREATE".equals(registerRequest.getEmailCode());
        
        // 只有非管理员创建的用户才需要验证邮箱验证码
        if (!isAdminCreate && (registerRequest.getEmailCode() == null || !validateEmailCode(registerRequest.getEmail(), registerRequest.getEmailCode()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 邮箱验证码无效或已过期!"));
        }

        // 创建新用户
        try {
            UserDto userDto = userService.register(registerRequest);
            logger.info("用户注册成功: {}", registerRequest.getUsername());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.error("用户创建失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "用户创建失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/send-email-verification")
    public ResponseEntity<?> sendEmailVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 电子邮箱不能为空!"));
        }
        
        // 验证邮箱格式
        if (!inputValidationUtils.isValidEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "错误: 邮箱格式不正确!"));
        }
        
        try {
            // 生成随机验证码
            String verificationCode = verificationCodeService.generateVerificationCode();
            
            // 存储验证码
            verificationCodeService.storeVerificationCode(email, verificationCode);
            
            // 异步发送验证码邮件
            asyncEmailService.sendVerificationCodeEmailAsync(email, verificationCode);
            
            logger.info("验证码已发送到邮箱: {}", email);
            
            return ResponseEntity.ok(Map.of("message", "验证码已发送，请查收邮箱"));
        } catch (Exception e) {
            logger.error("发送验证码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "发送验证码失败: " + e.getMessage()));
        }
    }
    
    // 检查用户名是否已存在
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    // 验证邮箱验证码
    private boolean validateEmailCode(String email, String code) {
        return verificationCodeService.validateVerificationCode(email, code);
    }
} 