# 课程评价系统

## 项目说明
这是一个前后端分离的课程评价系统，使用Spring Boot作为后端，Vue.js作为前端，使用Neon PostgreSQL数据库进行数据存储。

## 运行步骤

### 1. 克隆项目
```bash
git clone <项目地址>
```

### 2. 数据库配置
- 系统使用Neon云托管的PostgreSQL数据库，无需本地安装数据库
- 数据库连接信息配置在`course-evaluation-system/backend/src/main/resources/application.properties`中

### 3. 启动后端
```bash
cd course-evaluation-system/backend
mvn spring-boot:run
```

### 4. 启动前端
```bash
cd course-evaluation-system/frontend
npm install
npm run serve
```

### 5. 访问项目
- 前端：http://localhost:8080
- 后端：http://localhost:8088/api

## 使用的程序及版本
- **数据库**: Neon PostgreSQL 17.4
- **Node.js**: 14.17.0以上版本
- **npm**: 6.14.13以上版本
- **Maven**: 3.8.1以上版本
- **Java**: JDK 21
- **Spring Boot**: 3.2.4
- **Vue.js**: 3.2.0

## 数据库说明
- 使用Neon提供的云托管PostgreSQL服务
- 所有数据持久化存储在云端
- 应用启动时不会重新初始化数据库（配置了`spring.jpa.hibernate.ddl-auto=none`和`spring.sql.init.mode=never`）
- 数据库自动备份，提供高可用性

## 注意事项
- 不再需要本地安装和配置MySQL
- 确保有稳定的网络连接以访问Neon云数据库
- 确保已安装Node.js和npm
- 确保已安装Maven和JDK 21 