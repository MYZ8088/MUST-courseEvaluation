# 🚀 部署和CI/CD配置指南

## 📋 敏感信息清理完成

已成功清理所有敏感信息：
- ✅ 移除数据库连接字符串和密码的默认值
- ✅ 移除JWT密钥的默认值  
- ✅ 移除邮件账户密码的默认值
- ✅ 所有敏感信息改为环境变量方式

## 🔐 GitHub Secrets配置

在GitHub仓库中，依次进入 `Settings` → `Secrets and variables` → `Actions`，添加以下secrets：

### 必需的Production Secrets：
```
DATABASE_URL=jdbc:postgresql://your-neon-host/your-database
DATABASE_USERNAME=your-username  
DATABASE_PASSWORD=your-secure-password
JWT_SECRET=your-base64-encoded-secret-key
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your-email@domain.com
MAIL_PASSWORD=your-email-password
CORS_ALLOWED_ORIGINS=https://your-domain.com
COOKIE_SECURE=true
SHOW_SQL=false
```

### JWT密钥生成方法：
```bash
# 方法1：使用OpenSSL（推荐）
openssl rand -base64 64

# 方法2：使用在线生成器
# 访问 https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
# 选择256-bit密钥
```

## 🔄 CI/CD工作流说明

### 1. 主要CI/CD流程 (`.github/workflows/ci-cd.yml`)
**触发条件：**
- Push到main/develop分支
- 对main分支的Pull Request

**流程步骤：**
```
测试阶段 → 构建阶段 → 部署阶段
   ↓           ↓          ↓
单元测试    打包应用   生产部署
前端构建    存储artifacts  (需配置)
```

### 2. 安全扫描流程 (`.github/workflows/security-scan.yml`)
**触发条件：**
- 代码推送时自动扫描
- 每周一凌晨2点定时扫描

**安全检查项：**
- 依赖漏洞扫描（OWASP）
- 代码质量分析（CodeQL）
- 密钥泄露检测（TruffleHog）

## 🛠 本地开发环境配置

### 1. 环境变量配置
```bash
# 复制环境变量模板
cp .env.example .env

# 编辑.env文件，填写实际配置
nano .env
```

### 2. 启动应用
```bash
# 后端启动（确保已设置环境变量）
cd course-evaluation-system/backend
mvn spring-boot:run

# 前端启动
cd course-evaluation-system/frontend  
npm install
npm run serve
```

## 🌐 生产环境部署步骤

### 1. 服务器环境准备
```bash
# 安装Java 21
sudo apt update
sudo apt install openjdk-21-jdk

# 安装Node.js 18+
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装Nginx（可选，用于反向代理）
sudo apt install nginx
```

### 2. 环境变量配置
```bash
# 创建环境变量文件
sudo nano /etc/environment

# 添加以下内容：
DATABASE_URL=jdbc:postgresql://your-host:5432/your-db
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password
JWT_SECRET=your-jwt-secret
# ... 其他环境变量
```

### 3. 应用部署
```bash
# 克隆代码
git clone https://github.com/MYZ8088/DmustEvaluation.git
cd DmustEvaluation

# 构建后端
cd course-evaluation-system/backend
mvn clean package -DskipTests

# 构建前端
cd ../frontend
npm install
npm run build

# 启动后端服务
java -jar target/course-evaluation-*.jar &

# 配置Nginx部署前端
sudo cp -r dist/* /var/www/html/
```

## 🔧 Docker部署（可选）

### 1. 后端Dockerfile
```dockerfile
FROM openjdk:21-jre-slim
COPY target/course-evaluation-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 2. 前端Dockerfile
```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3. Docker Compose
```yaml
version: '3.8'
services:
  backend:
    build: ./course-evaluation-system/backend
    ports:
      - "8088:8088"
    environment:
      - DATABASE_URL=${DATABASE_URL}
      - DATABASE_USERNAME=${DATABASE_USERNAME}
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
      # ... 其他环境变量
  
  frontend:
    build: ./course-evaluation-system/frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

## 🚨 安全注意事项

### 1. 生产环境必做：
- ✅ 所有敏感信息使用环境变量
- ✅ 启用HTTPS（设置COOKIE_SECURE=true）
- ✅ 配置防火墙规则
- ✅ 定期更新依赖
- ✅ 监控安全扫描报告

### 2. 禁止的操作：
- ❌ 不要在代码中硬编码密码
- ❌ 不要提交.env文件到git
- ❌ 不要在生产环境开启SQL日志
- ❌ 不要使用弱JWT密钥

## 📊 监控和维护

### 1. 健康检查端点：
- `GET /api/system/health` - 系统健康状态
- `GET /api/system/info` - 应用信息
- `GET /api/actuator/health` - 详细健康信息

### 2. 日志监控：
```bash
# 查看应用日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log
```

### 3. 数据库备份：
```bash
# 手动创建备份（需要管理员权限）
curl -X POST "https://your-domain.com/api/system/backup" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"

# 验证数据完整性
curl -X POST "https://your-domain.com/api/system/validate" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

## 🎯 故障排除

### 常见问题：
1. **应用启动失败** → 检查环境变量是否正确设置
2. **数据库连接失败** → 验证数据库URL和凭证
3. **JWT验证失败** → 确认JWT_SECRET已正确配置
4. **邮件发送失败** → 检查邮件服务器配置

### 调试命令：
```bash
# 检查环境变量
printenv | grep -E "(DATABASE|JWT|MAIL)"

# 检查端口占用
netstat -tlnp | grep 8088

# 检查Java进程
jps -l
```

---

🎉 **配置完成后，你的项目就可以安全地推送到GitHub，并具备完整的CI/CD能力！** 