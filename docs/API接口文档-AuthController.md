# 认证接口文档 (AuthController)

## 基础信息

- **基础路径**: `/api/auth`
- **Controller**: `AuthController`
- **说明**: 提供用户注册、登录、登出、刷新Token、获取当前用户信息等功能

---

## 1. 用户注册

### 接口信息

- **路径**: `POST /api/auth/register`
- **说明**: 用户注册，创建新账号
- **认证**: 无需登录

### 请求参数

**请求体 (JSON)**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名/登录账号 |
| password | String | 是 | 密码（明文，后端会自动加密） |
| email | String | 否 | 用户邮箱 |
| phone | String | 否 | 手机号 |

**请求示例**:
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "testuser",
    "avatarUrl": null,
    "role": "USER",
    "email": "test@example.com",
    "phone": "13800138000"
  }
}
```

**UserVO 字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| avatarUrl | String | 用户头像URL |
| role | String | 用户角色：USER（普通用户）、ADMIN（管理员）、REVIEWER（审核员） |
| email | String | 邮箱 |
| phone | String | 手机号 |

**错误响应**:

```json
{
  "code": 40000,
  "message": "请求参数错误",
  "data": null
}
```

```json
{
  "code": 50001,
  "message": "用户名已存在",
  "data": null
}
```

### 注意事项

1. 用户名不能为空
2. 密码不能为空
3. 用户名必须唯一，如果已存在会返回错误
4. 注册成功后，用户角色默认为 `USER`（普通用户）
5. 密码会在后端自动加密存储

---

## 2. 用户登录

### 接口信息

- **路径**: `POST /api/auth/login`
- **说明**: 用户登录，返回JWT Token
- **认证**: 无需登录

### 请求参数

**请求体 (JSON)**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名/登录账号 |
| password | String | 是 | 密码（明文） |

**请求示例**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbklkIjoxLCJleHAiOjE3MDAwMDAwMDB9.xxx",
    "refreshToken": null
  }
}
```

**TokenResponse 字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| accessToken | String | 访问令牌（JWT），用于后续请求认证 |
| refreshToken | String | 刷新令牌（当前为null，预留字段） |

**错误响应**:

```json
{
  "code": 40400,
  "message": "用户不存在",
  "data": null
}
```

```json
{
  "code": 40101,
  "message": "账号或密码错误",
  "data": null
}
```

### 注意事项

1. 登录成功后，需要将 `accessToken` 保存到本地（如 localStorage）
2. 后续需要认证的请求，需要在请求头中携带 Token：
   ```
   Authorization: Bearer <accessToken>
   ```
3. Token 有效期为 900 秒（15分钟）
4. Token 过期后需要调用刷新接口或重新登录

---

## 3. 用户登出

### 接口信息

- **路径**: `POST /api/auth/logout`
- **说明**: 用户登出，清除服务端Token
- **认证**: 需要登录

### 请求参数

无需请求参数，但需要在请求头中携带 Token。

**请求头**:
```
Authorization: Bearer <accessToken>
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": null
}
```

**错误响应**:

```json
{
  "code": 40101,
  "message": "未登录或无权限",
  "data": null
}
```

### 注意事项

1. 登出后，服务端会清除Token
2. 前端也应该清除本地存储的Token
3. 登出后需要重新登录才能访问需要认证的接口

---

## 4. 刷新访问令牌

### 接口信息

- **路径**: `POST /api/auth/refresh`
- **说明**: 刷新当前用户的访问令牌，获取新的Token
- **认证**: 需要登录

### 请求参数

无需请求参数，但需要在请求头中携带 Token。

**请求头**:
```
Authorization: Bearer <accessToken>
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbklkIjoxLCJleHAiOjE3MDAwMDAwMDB9.xxx",
    "refreshToken": null
  }
}
```

**错误响应**:

```json
{
  "code": 40101,
  "message": "未登录或无权限",
  "data": null
}
```

### 注意事项

1. 刷新Token时，会先使旧Token失效，然后生成新Token
2. 刷新成功后，需要使用新的Token替换旧的Token
3. 建议在Token即将过期前（如剩余1-2分钟）自动刷新
4. 刷新失败时，应该跳转到登录页面

---

## 5. 获取当前登录用户信息

### 接口信息

- **路径**: `GET /api/auth/me`
- **说明**: 获取当前登录用户的详细信息
- **认证**: 需要登录

### 请求参数

无需请求参数，但需要在请求头中携带 Token。

**请求头**:
```
Authorization: Bearer <accessToken>
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "testuser",
    "avatarUrl": null,
    "role": "ADMIN",
    "email": "test@example.com",
    "phone": "13800138000"
  }
}
```

**UserVO 字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| avatarUrl | String | 用户头像URL |
| role | String | 用户角色：USER、ADMIN、REVIEWER |
| email | String | 邮箱 |
| phone | String | 手机号 |

**错误响应**:

```json
{
  "code": 40101,
  "message": "未登录或无权限",
  "data": null
}
```

```json
{
  "code": 40400,
  "message": "请求数据不存在",
  "data": null
}
```

### 注意事项

1. 此接口用于获取当前登录用户的信息
2. 可以根据返回的 `role` 字段判断用户权限
3. 前端可以根据用户角色显示不同的功能菜单

---

## 通用错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40101 | 未登录或无权限 |
| 40400 | 请求数据不存在 |
| 50000 | 系统内部异常 |
| 50001 | 操作失败 |

---

## 完整请求示例

### 使用 Axios

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://your-server-domain/api',
  timeout: 10000
});

// 1. 注册
const register = async () => {
  const response = await api.post('/auth/register', {
    username: 'testuser',
    password: '123456',
    email: 'test@example.com'
  });
  console.log(response.data);
};

// 2. 登录
const login = async () => {
  const response = await api.post('/auth/login', {
    username: 'testuser',
    password: '123456'
  });
  const token = response.data.data.accessToken;
  localStorage.setItem('token', token);
  return token;
};

// 3. 获取当前用户信息（需要Token）
const getCurrentUser = async () => {
  const token = localStorage.getItem('token');
  const response = await api.get('/auth/me', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  console.log(response.data);
};

// 4. 刷新Token
const refreshToken = async () => {
  const token = localStorage.getItem('token');
  const response = await api.post('/auth/refresh', {}, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const newToken = response.data.data.accessToken;
  localStorage.setItem('token', newToken);
  return newToken;
};

// 5. 登出
const logout = async () => {
  const token = localStorage.getItem('token');
  await api.post('/auth/logout', {}, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  localStorage.removeItem('token');
};
```

### 使用 Fetch

```javascript
// 登录
const login = async () => {
  const response = await fetch('http://your-server-domain/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      username: 'testuser',
      password: '123456'
    })
  });
  const data = await response.json();
  if (data.code === 0) {
    localStorage.setItem('token', data.data.accessToken);
  }
};

// 获取当前用户信息
const getCurrentUser = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://your-server-domain/api/auth/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const data = await response.json();
  console.log(data);
};
```

---

## 用户角色说明

| 角色 | 值 | 说明 |
|------|-----|------|
| 普通用户 | USER | 默认角色，只能查看题库 |
| 管理员 | ADMIN | 可以创建和更新题库 |
| 审核员 | REVIEWER | 可以审核题库（预留） |

---

## 注意事项总结

1. **注册和登录接口无需认证**，可以直接调用
2. **登出、刷新Token、获取用户信息需要认证**，必须在请求头中携带Token
3. **Token格式**：`Authorization: Bearer <token>`（注意Bearer后面有空格）
4. **Token有效期**：900秒（15分钟），过期后需要刷新或重新登录
5. **密码安全**：前端传输的是明文密码，后端会自动加密存储
6. **用户名唯一性**：注册时如果用户名已存在，会返回错误
7. **角色权限**：根据用户角色（role）判断是否有权限执行某些操作

