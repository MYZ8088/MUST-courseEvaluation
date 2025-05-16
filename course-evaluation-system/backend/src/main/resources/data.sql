-- 清空现有数据（注意顺序，先删除有外键引用的表）
TRUNCATE TABLE `reviews`;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `users`;
TRUNCATE TABLE `courses`;
TRUNCATE TABLE `teachers`;
TRUNCATE TABLE `faculties`;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据 (无moderator角色)
INSERT INTO `users` (`id`, `username`, `password`, `email`, `full_name`, `student_id`, `role`, `active`, `can_comment`) VALUES
(1, 'admin', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', 'admin@example.com', '系统管理员', NULL, 'ROLE_ADMIN', 1, 1),
(2, 'student1', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', 'student1@example.com', '张三', '2023001', 'ROLE_STUDENT', 1, 1),
(3, 'student2', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', 'student2@example.com', '李四', '2023002', 'ROLE_STUDENT', 1, 1);

-- 插入院系数据
INSERT INTO `faculties` (`id`, `name`, `description`) VALUES
(1, '计算机科学与技术学院', '培养计算机科学、软件工程、人工智能等领域的专业人才'),
(2, '电子信息工程学院', '专注于电子、通信、集成电路等领域的研究与教学'),
(3, '数学与统计学院', '数学理论与应用研究，培养数学、统计、数据科学等方向的人才');

-- 插入教师数据
INSERT INTO `teachers` (`id`, `name`, `title`, `email`, `bio`, `faculty_id`) VALUES
(1, '王教授', '教授', 'wang@example.com', '计算机科学专家，研究方向为人工智能与机器学习', 1),
(2, '李教授', '副教授', 'li@example.com', '软件工程专家，研究方向为软件测试与质量保证', 1),
(3, '张教授', '教授', 'zhang@example.com', '微电子学专家，研究方向为集成电路设计', 2),
(4, '刘教授', '教授', 'liu@example.com', '应用数学专家，研究方向为数据分析与最优化', 3);

-- 插入课程数据
INSERT INTO `courses` (`id`, `code`, `name`, `credits`, `description`, `type`, `assessment_criteria`, `faculty_id`, `teacher_id`) VALUES
(1, 'CS101', '计算机导论', 3.0, '计算机科学基础课程，涵盖计算机组成原理、算法基础等内容', 'COMPULSORY', '平时作业30%，小组项目30%，期末考试40%', 1, 1),
(2, 'CS201', '数据结构与算法', 4.0, '讲解常用数据结构与算法设计，培养学生的编程与算法思维', 'COMPULSORY', '编程作业40%，期中考试20%，期末考试40%', 1, 2),
(3, 'EE101', '电路基础', 3.5, '电子工程入门课程，介绍基本电路原理与分析方法', 'COMPULSORY', '实验报告30%，中期测验30%，期末考试40%', 2, 3),
(4, 'MATH301', '高等数学', 5.0, '微积分、线性代数等数学基础知识与应用', 'COMPULSORY', '习题课20%，期中测验30%，期末考试50%', 3, 4),
(5, 'CS301', '软件工程', 3.0, '软件开发流程、项目管理、测试与维护等内容', 'ELECTIVE', '团队项目60%，案例分析20%，期末测验20%', 1, 2);

-- 插入评价数据
INSERT INTO `reviews` (`id`, `content`, `rating`, `anonymous`, `status`, `user_id`, `course_id`, `created_at`) VALUES
(1, '老师讲得非常好，课程内容安排合理，收获很大', 5, 0, 'APPROVED', 2, 1, '2023-10-15 14:30:00'),
(2, '内容有些难，但是通过课后练习可以很好地掌握', 4, 0, 'APPROVED', 3, 1, '2023-10-20 16:45:00'),
(3, '实验环节很有趣，增强了对理论知识的理解', 5, 1, 'APPROVED', 2, 3, '2023-11-05 10:20:00'),
(4, '课程内容有些过时，希望能更新一些实用的知识', 3, 0, 'APPROVED', 3, 2, '2023-11-10 09:15:00'),
(5, '教授很负责，答疑解惑很耐心，但作业量有点大', 4, 1, 'APPROVED', 2, 5, '2023-12-01 11:30:00'); 