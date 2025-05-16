package com.must.courseevaluation.config;

import com.must.courseevaluation.model.User;
import com.must.courseevaluation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员用户
        logger.info("开始初始化数据...");
        logger.info("检查用户表是否有数据...");
        
        if (userRepository.count() == 0) {
            logger.info("用户表为空，开始创建初始用户数据");
            
            // 创建管理员用户
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setEmail("admin@must.edu.mo");
            adminUser.setFullName("系统管理员");
            adminUser.setRole(User.Role.ROLE_ADMIN);
            adminUser.setActive(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            
            userRepository.save(adminUser);
            
            logger.info("初始化数据: 已创建管理员用户 (username: admin, password: 123456)");
            
            // 创建一个普通学生用户供测试
            User studentUser = new User();
            studentUser.setUsername("student");
            studentUser.setPassword(passwordEncoder.encode("123456"));
            studentUser.setEmail("student@must.edu.mo");
            studentUser.setFullName("测试学生");
            studentUser.setStudentId("2024001");
            studentUser.setRole(User.Role.ROLE_STUDENT);
            studentUser.setActive(true);
            studentUser.setCreatedAt(LocalDateTime.now());
            
            userRepository.save(studentUser);
            
            logger.info("初始化数据: 已创建学生用户 (username: student, password: 123456)");
            logger.info("数据初始化完成！");
        } else {
            logger.info("用户表已有数据，跳过初始化步骤");
            logger.info("当前用户数量: {}", userRepository.count());
            
            // 添加以下代码打印所有用户和密码的前8个字符，用于排查问题
            userRepository.findAll().forEach(user -> {
                logger.info("已有用户: {} ({}), 密码前缀: {}", 
                          user.getUsername(), 
                          user.getRole(), 
                          user.getPassword().substring(0, Math.min(8, user.getPassword().length())));
                
                // 测试123456是否匹配
                boolean matches = passwordEncoder.matches("123456", user.getPassword());
                logger.info("用户 {} 密码'123456'是否匹配: {}", user.getUsername(), matches);
                
                // 如果admin用户密码不匹配123456，更新密码
                if (user.getUsername().equals("admin") && !matches) {
                    String encodedPassword = passwordEncoder.encode("123456");
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    logger.info("已更新admin用户密码为123456，新密码哈希: {}", encodedPassword);
                }
            });

            // 检查student1和student2账号是否存在
            List<String> studentUsernames = Arrays.asList("student1", "student2");
            for (String username : studentUsernames) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    boolean matches = passwordEncoder.matches("123456", user.getPassword());
                    if (!matches) {
                        String encodedPassword = passwordEncoder.encode("123456");
                        user.setPassword(encodedPassword);
                        userRepository.save(user);
                        logger.info("已更新用户 {} 密码为123456，新密码哈希: {}", username, encodedPassword);
                    } else {
                        logger.info("用户 {} 密码已正确设置为123456", username);
                    }
                } else {
                    logger.info("未找到用户: {}，尝试创建该用户", username);
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(passwordEncoder.encode("123456"));
                    newUser.setEmail(username + "@must.edu.mo");
                    newUser.setFullName(username.equals("student1") ? "张三" : "李四");
                    newUser.setStudentId(username.equals("student1") ? "2024001" : "2024002");
                    newUser.setRole(User.Role.ROLE_STUDENT);
                    newUser.setActive(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    userRepository.save(newUser);
                    logger.info("已创建用户: {} (密码: 123456)", username);
                }
            }
        }
    }
} 