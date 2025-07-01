# 🎓 澳门科技大学课程评价系统

> 一个现代化的前后端分离课程评价平台，集成了安全审计、数据备份、实时监控等企业级功能

## ✨ 项目特色

### 🚀 核心功能
- **课程评价管理** - 匿名评价、评分统计、内容审核
- **用户权限系统** - 学生、教师两级权限
- **教师课程管理** - 教师信息、课程关联、评价查看
- **学院系统管理** - 学院分类、课程归属、数据统计

### 🛡️ 安全特性
- **安全审计系统** - 登录监控、可疑活动检测、安全事件记录
- **JWT身份验证** - 无状态认证、自动过期、安全传输
- **数据完整性验证** - 自动检查数据一致性、关联完整性
- **敏感信息保护** - 环境变量配置、密码加密存储

### 📊 监控与备份
- **实时健康监控** - 系统状态、数据库连接、性能指标
- **自动数据备份** - Neon云备份 + 应用层验证
- **日志审计追踪** - 结构化日志、安全事件记录
- **性能监控集成** - Actuator + Prometheus指标

### 🔄 DevOps集成
- **GitHub Actions CI/CD** - 自动测试、构建、部署
- **安全扫描流程** - 依赖检查、代码分析、密钥扫描
- **Docker化部署** - 容器化支持、多环境配置

## 🏗️ 技术栈

### 后端技术
- **Spring Boot 3.2.4** - 主框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问层
- **JWT (jsonwebtoken 0.11.5)** - 身份验证
- **Neon PostgreSQL** - 云数据库
- **Spring Boot Actuator** - 应用监控
- **Micrometer + Prometheus** - 指标收集

### 前端技术
- **Vue.js 3.4** - 前端框架
- **Vue Router** - 路由管理
- **Vuex** - 状态管理
- **Axios** - HTTP客户端

### 开发工具
- **Java 21** - 开发语言
- **Maven 3.8+** - 构建工具
- **Node.js 18+** - 前端运行环境
- **GitHub Actions** - CI/CD流水线

## 🚀 快速开始

### 环境要求
- Java 21+
- Node.js 18+
- Maven 3.8+
- Git

### 1. 克隆项目
```bash
git clone https://github.com/MYZ8088/DmustEvaluation.git
cd DmustEvaluation
```

### 2. 环境变量配置
```bash
# 复制环境变量模板
cp course-evaluation-system/.env.example course-evaluation-system/.env

# 编辑环境变量（必须配置）
nano course-evaluation-system/.env
```

**必需的环境变量：**
```bash
# 数据库配置
DATABASE_URL=jdbc:postgresql://your-host:5432/your-database
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password

# JWT安全配置
JWT_SECRET=your-base64-encoded-secret-key

# 邮件服务配置
MAIL_HOST=smtp.qq.com
MAIL_USERNAME=your-email@domain.com
MAIL_PASSWORD=your-email-password

# 安全配置
CORS_ALLOWED_ORIGINS=http://localhost:8080,http://127.0.0.1:8080
```

### 3. 启动后端服务
```bash
cd course-evaluation-system/backend

# 设置环境变量（Windows）
set DATABASE_URL=your-database-url
set DATABASE_USERNAME=your-username
set DATABASE_PASSWORD=your-password
set JWT_SECRET=your-jwt-secret

# 启动应用
mvn spring-boot:run
```

### 4. 启动前端服务
```bash
cd course-evaluation-system/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

### 5. 访问应用
- **前端界面**: http://localhost:8080
- **后端API**: http://localhost:8088/api
- **健康检查**: http://localhost:8088/api/system/health
- **应用监控**: http://localhost:8088/api/actuator/health

## 🔧 管理功能

### 系统监控
```bash
# 系统健康状态
GET /api/system/health

# 应用信息
GET /api/system/info

# 数据库统计
GET /api/system/stats
```

### 数据备份（管理员专用）
```bash
# 创建手动备份
POST /api/system/backup?name=backup_name

# 查看备份列表
GET /api/system/backup/list

# 数据完整性验证
POST /api/system/validate

# 清理过期备份
DELETE /api/system/backup/cleanup?daysToKeep=7
```

## 🛡️ 安全特性

### 访问控制
- **学生用户** - 查看课程、提交评价、管理个人资料
- **教师用户** - 查看自己的课程评价、管理课程信息
- **管理员用户** - 系统管理、用户管理、数据备份、安全审计

### 安全监控
- 登录尝试记录（成功/失败）
- 可疑活动自动检测
- IP地址和设备追踪
- 安全事件实时告警

### 数据保护
- 密码BCrypt加密存储
- JWT令牌自动过期机制
- HTTPS传输加密
- 敏感数据脱敏处理

## 📊 生产部署

### Docker部署（推荐）
```bash
# 构建并启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 手动部署
```bash
# 构建后端
cd course-evaluation-system/backend
mvn clean package -DskipTests

# 构建前端
cd ../frontend
npm install
npm run build

# 启动后端服务
java -jar target/course-evaluation-*.jar

# 部署前端到Web服务器
cp -r dist/* /var/www/html/
```

### 环境配置
生产环境需要配置以下环境变量：
- `DATABASE_URL` - 生产数据库连接
- `JWT_SECRET` - 强随机密钥
- `MAIL_*` - 邮件服务配置
- `CORS_ALLOWED_ORIGINS` - 允许的前端域名
- `COOKIE_SECURE=true` - 启用安全Cookie

## 🔄 CI/CD流程

项目集成了GitHub Actions自动化流程：

### 自动测试
- 代码推送时自动运行单元测试
- 前后端构建验证
- 依赖安全扫描

### 安全检查
- OWASP依赖漏洞扫描
- CodeQL代码质量分析
- TruffleHog密钥泄露检测

### 自动部署
- main分支自动部署到生产环境
- develop分支部署到测试环境

## 📖 API文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出

### 课程管理
- `GET /api/courses` - 获取课程列表
- `GET /api/courses/{id}` - 获取课程详情
- `POST /api/courses` - 创建课程（管理员）

### 评价管理
- `GET /api/reviews` - 获取评价列表
- `POST /api/reviews` - 提交评价
- `PUT /api/reviews/{id}` - 更新评价

### 系统管理
- `GET /api/system/health` - 系统健康检查
- `POST /api/system/backup` - 创建数据备份
- `POST /api/system/validate` - 数据完整性验证

## 🔍 故障排除

### 常见问题

1. **应用启动失败**
   ```bash
   # 检查环境变量
   echo $DATABASE_URL
   
   # 检查Java版本
   java -version
   ```

2. **数据库连接失败**
   ```bash
   # 测试数据库连接
   curl -X GET "http://localhost:8088/api/system/health"
   ```

3. **前端无法访问后端**
   ```bash
   # 检查CORS配置
   echo $CORS_ALLOWED_ORIGINS
   ```

### 日志查看
```bash
# 应用日志
tail -f course-evaluation-system/backend/logs/application.log

# 错误日志
grep ERROR course-evaluation-system/backend/logs/application.log
```

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/新功能`)
3. 提交更改 (`git commit -am '添加新功能'`)
4. 推送到分支 (`git push origin feature/新功能`)
5. 创建Pull Request

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

## 📞 联系方式

- **项目维护者**: MYZ8088
- **GitHub**: https://github.com/MYZ8088/DmustEvaluation
- **问题报告**: [GitHub Issues](https://github.com/MYZ8088/DmustEvaluation/issues)

---

## 🎉 更新日志

### v1.0.0 (2024-12)
- ✅ 完整的课程评价系统
- ✅ 安全审计和监控系统
- ✅ 数据备份和完整性验证
- ✅ CI/CD自动化流程
- ✅ Docker化部署支持
- ✅ 企业级安全特性

**⭐ 如果这个项目对你有帮助，请给个Star支持一下！** 