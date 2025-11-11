# 配置文件说明

## 配置文件结构

项目使用 Spring Boot 的多环境配置，敏感信息（数据库、Redis等）单独配置，不会提交到Git。

### 配置文件说明

1. **application.yml** - 主配置文件（已提交到Git）
   - 包含非敏感配置
   - 使用环境变量或 profile 特定配置覆盖敏感信息

2. **application-local.yml** - 本地开发环境配置（不提交到Git）
   - 包含本地数据库、Redis等敏感信息
   - 需要手动创建

3. **application-prod.yml** - 生产环境配置（不提交到Git）
   - 包含生产环境数据库、Redis等敏感信息
   - 需要手动创建

4. **application-*.yml.example** - 配置模板文件（已提交到Git）
   - 提供配置示例，不包含真实密码

## 配置方式

### 方式一：使用配置文件（推荐）

1. 复制配置模板：
   ```bash
   # 本地开发
   cp src/main/resources/application-local.yml src/main/resources/application-local.yml
   
   # 生产环境
   cp src/main/resources/application-prod.yml src/main/resources/application-prod.yml
   ```

2. 编辑配置文件，填写实际的服务器地址和密码：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://your_host:3306/mianshiyuan?...
       username: your_username
       password: your_password
     data:
       redis:
         host: your_redis_host
         password: your_redis_password
   ```

3. 激活配置：
   - 本地开发：`spring.profiles.active=local`（application.yml中已默认）
   - 生产环境：通过环境变量 `SPRING_PROFILES_ACTIVE=prod`

### 方式二：使用环境变量（推荐用于生产环境）

在启动应用时设置环境变量：

```bash
# Linux/Mac
export DB_URL="jdbc:mysql://your_host:3306/mianshiyuan?..."
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"
export REDIS_HOST="your_redis_host"
export REDIS_PASSWORD="your_redis_password"

# Windows
set DB_URL=jdbc:mysql://your_host:3306/mianshiyuan?...
set DB_USERNAME=your_username
set DB_PASSWORD=your_password
set REDIS_HOST=your_redis_host
set REDIS_PASSWORD=your_redis_password
```

### 方式三：使用启动参数

```bash
java -jar app.jar \
  --spring.datasource.url=jdbc:mysql://your_host:3306/mianshiyuan?... \
  --spring.datasource.username=your_username \
  --spring.datasource.password=your_password \
  --spring.data.redis.host=your_redis_host \
  --spring.data.redis.password=your_redis_password
```

## 环境变量列表

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| SPRING_PROFILES_ACTIVE | 激活的配置环境 | local |
| DB_URL | 数据库连接URL | jdbc:mysql://localhost:3306/mianshiyuan?... |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | 空 |
| REDIS_HOST | Redis服务器地址 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| REDIS_PASSWORD | Redis密码 | 空 |
| REDIS_DATABASE | Redis数据库编号 | 0 |

## 安全提示

1. ✅ **application-local.yml 和 application-prod.yml 已添加到 .gitignore**
2. ✅ **不要将包含真实密码的配置文件提交到Git**
3. ✅ **生产环境建议使用环境变量或配置中心**
4. ✅ **定期更换密码**
5. ✅ **使用强密码**

## 快速开始

1. 复制配置模板：
   ```bash
   cp src/main/resources/application-local.yml src/main/resources/application-local.yml
   ```

2. 编辑 `application-local.yml`，填写你的配置

3. 启动应用，配置会自动加载

