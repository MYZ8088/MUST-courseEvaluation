# 安全配置指南

## 🚨 紧急安全配置

### 1. 环境变量配置
在生产环境中，请设置以下环境变量（不要在代码中硬编码）：

```bash
# 数据库配置
export DATABASE_URL="jdbc:postgresql://your-host/your-database"
export DATABASE_USERNAME="your-username"
export DATABASE_PASSWORD="your-secure-password"

# JWT配置 - 使用256位以上的强密钥
export JWT_SECRET="your-base64-encoded-secret-key-minimum-256-bits"
export JWT_EXPIRATION="86400000"

# 邮件配置
export MAIL_HOST="smtp.qq.com"
export MAIL_PORT="587"
export MAIL_USERNAME="your-email@domain.com"
export MAIL_PASSWORD="your-email-password"

# CORS配置 - 指定具体的前端域名
export CORS_ALLOWED_ORIGINS="https://your-frontend-domain.com,https://your-app-domain.com"

# 安全配置
export COOKIE_SECURE="true"
export SHOW_SQL="false"
```

### 2. JWT密钥生成
使用以下命令生成安全的JWT密钥：

```bash
# 生成256位随机密钥并转换为Base64
openssl rand -base64 32
```

### 3. 数据库连接安全
- 使用专用数据库用户，不要使用管理员账户
- 启用SSL连接（已配置）
- 定期更换数据库密码

### 4. 监控端点访问
- `/actuator/health` - 公开健康检查
- `/actuator/info` - 公开应用信息
- `/actuator/**` - 管理员专用监控端点

## 📊 应用监控

### 健康检查URL
- 基础健康检查: `http://localhost:8088/api/actuator/health`
- 详细健康信息: 需要管理员权限
- 应用信息: `http://localhost:8088/api/actuator/info`

### 监控指标
系统会自动收集以下指标：
- 数据库连接池状态
- JVM内存使用情况
- HTTP请求统计
- 系统磁盘空间
- 邮件服务状态

## 🔒 安全建议

### 立即执行
1. 删除application.properties中的硬编码密码
2. 设置环境变量
3. 更新CORS配置为具体域名
4. 启用HTTPS (设置COOKIE_SECURE=true)

### 定期维护
1. 定期更换JWT密钥
2. 监控异常登录尝试
3. 审查访问日志
4. 更新依赖版本 