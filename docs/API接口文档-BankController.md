# 题库接口文档 (BankController)

## 基础信息

- **基础路径**: `/api/bank`
- **Controller**: `BankController`
- **说明**: 提供题库的创建、更新、查询等功能

---

## 1. 创建题库

### 接口信息

- **路径**: `POST /api/bank`
- **说明**: 创建新的题库，支持保存草稿或提交审核两种模式
- **认证**: 需要登录 + 管理员权限（ADMIN角色）

### 请求参数

**请求头**:
```
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**请求体 (JSON)**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 题库名称 |
| description | String | 否 | 题库简介 |
| tagList | List<String> | 否 | 标签列表，JSON数组格式 |
| submitForReview | Boolean | 否 | 是否提交审核，默认false<br/>- `false`: 保存草稿（状态=0）<br/>- `true`: 提交审核（状态=1） |

**请求示例 - 保存草稿**:
```json
{
  "name": "Java核心面试题",
  "description": "涵盖Java基础、集合框架、多线程、JVM等核心知识点",
  "tagList": ["Java", "基础", "并发", "数据结构"],
  "submitForReview": false
}
```

**请求示例 - 提交审核**:
```json
{
  "name": "Java核心面试题",
  "description": "涵盖Java基础、集合框架、多线程、JVM等核心知识点",
  "tagList": ["Java", "基础", "并发", "数据结构"],
  "submitForReview": true
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
    "name": "Java核心面试题",
    "description": "涵盖Java基础、集合框架、多线程、JVM等核心知识点",
    "tagList": ["Java", "基础", "并发", "数据结构"],
    "creatorId": 1,
    "status": 0,
    "reviewId": null,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

**BankVO 字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 题库ID |
| name | String | 题库名称 |
| description | String | 题库简介 |
| tagList | List<String> | 标签列表 |
| creatorId | Long | 创建人ID |
| status | Integer | 状态：0=草稿，1=待审，2=通过，3=驳回 |
| reviewId | Long | 最新审核记录ID |
| createdAt | Date | 创建时间 |
| updatedAt | Date | 更新时间 |

**错误响应**:

```json
{
  "code": 40000,
  "message": "题库名称不能为空",
  "data": null
}
```

```json
{
  "code": 40101,
  "message": "未登录或无权限",
  "data": null
}
```

```json
{
  "code": 40300,
  "message": "需要管理员权限",
  "data": null
}
```

```json
{
  "code": 50001,
  "message": "创建题库失败",
  "data": null
}
```

### 注意事项

1. **权限要求**：必须是管理员（ADMIN角色）才能创建题库
2. **题库名称**：必填字段，不能为空
3. **标签列表**：可选，如果为空或不传，默认为空数组 `[]`
4. **提交模式**：
   - `submitForReview = false`：保存为草稿，状态为 0
   - `submitForReview = true`：提交审核，状态为 1
5. **创建人**：自动设置为当前登录用户的ID

---

## 2. 更新题库

### 接口信息

- **路径**: `PUT /api/bank/{id}`
- **说明**: 更新指定ID的题库，支持保存草稿或提交审核两种模式
- **认证**: 需要登录 + 管理员权限（ADMIN角色）

### 请求参数

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 题库ID |

**请求头**:
```
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**请求体 (JSON)**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 题库名称 |
| description | String | 否 | 题库简介 |
| tagList | List<String> | 否 | 标签列表，JSON数组格式 |
| submitForReview | Boolean | 否 | 是否提交审核，默认false<br/>- `false`: 保存草稿（状态=0）<br/>- `true`: 提交审核（状态=1） |

**请求示例 - 保存草稿**:
```json
{
  "name": "Java核心面试题（更新）",
  "description": "更新后的描述",
  "tagList": ["Java", "高级", "实战"],
  "submitForReview": false
}
```

**请求示例 - 提交审核**:
```json
{
  "name": "Java核心面试题（更新）",
  "description": "更新后的描述",
  "tagList": ["Java", "高级", "实战"],
  "submitForReview": true
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
    "name": "Java核心面试题（更新）",
    "description": "更新后的描述",
    "tagList": ["Java", "高级", "实战"],
    "creatorId": 1,
    "status": 0,
    "reviewId": null,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T11:00:00"
  }
}
```

**错误响应**:

```json
{
  "code": 40400,
  "message": "题库不存在",
  "data": null
}
```

```json
{
  "code": 40101,
  "message": "未登录或无权限",
  "data": null
}
```

```json
{
  "code": 40300,
  "message": "需要管理员权限",
  "data": null
}
```

```json
{
  "code": 50001,
  "message": "更新题库失败",
  "data": null
}
```

### 注意事项

1. **权限要求**：必须是管理员（ADMIN角色）才能更新题库
2. **任何状态都可以更新**：无论题库是草稿、待审、通过还是驳回状态，都可以更新
3. **更新后状态**：根据 `submitForReview` 字段重新设置状态
   - `submitForReview = false`：状态变为 0（草稿）
   - `submitForReview = true`：状态变为 1（待审）
4. **题库名称**：必填字段，不能为空
5. **标签列表**：可选，如果为空或不传，默认为空数组 `[]`

---

## 3. 根据ID查询题库

### 接口信息

- **路径**: `GET /api/bank/{id}`
- **说明**: 根据题库ID查询题库详细信息
- **认证**: 无需登录

### 请求参数

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 题库ID |

**请求示例**:
```
GET /api/bank/1
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "name": "Java核心面试题",
    "description": "涵盖Java基础、集合框架、多线程、JVM等核心知识点",
    "tagList": ["Java", "基础", "并发", "数据结构"],
    "creatorId": 1,
    "status": 2,
    "reviewId": 10,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
}
```

**错误响应**:

```json
{
  "code": 40400,
  "message": "题库不存在",
  "data": null
}
```

### 注意事项

1. **无需认证**：此接口不需要登录，可以直接访问
2. **返回完整信息**：包括题库的所有字段信息
3. **标签格式**：返回的 `tagList` 是数组格式，不是JSON字符串

---

## 4. 查询所有标签

### 接口信息

- **路径**: `GET /api/bank/tags`
- **说明**: 查询所有题库的标签列表，去重后按包含该标签的题库数量降序排列
- **认证**: 无需登录

### 请求参数

无需请求参数。

**请求示例**:
```
GET /api/bank/tags
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    "后端",
    "高级",
    "基础",
    "Java",
    "系统设计",
    "实战",
    "数据库",
    "并发",
    "框架",
    "数据结构",
    "Python",
    "JavaScript",
    "前端",
    "算法",
    "网络"
  ]
}
```

**响应说明**:

- 返回的是标签名称的数组
- 标签已去重
- 按包含该标签的题库数量降序排列（使用频率高的标签在前）

**错误响应**:

```json
{
  "code": 50000,
  "message": "系统内部异常",
  "data": null
}
```

### 注意事项

1. **无需认证**：此接口不需要登录，可以直接访问
2. **排序规则**：按包含该标签的题库数量降序排列
3. **去重处理**：相同标签只返回一次
4. **空标签处理**：空标签和null标签会被过滤掉
5. **使用场景**：可用于前端标签云、标签筛选下拉框等

### 业务逻辑说明

1. 查询所有题库记录
2. 提取每个题库的 `tagList`（JSON字符串转List）
3. 统计每个标签在所有题库中出现的次数
4. 按出现次数降序排序
5. 返回去重后的标签列表

**示例**：
- 假设有10个题库使用了"后端"标签
- 有8个题库使用了"高级"标签
- 有5个题库使用了"Java"标签
- 则返回顺序为：["后端", "高级", "Java", ...]

---

## 5. 分页查询题库列表

### 接口信息

- **路径**: `GET /api/bank`
- **说明**: 分页查询题库列表，支持按名称模糊搜索和按标签筛选
- **认证**: 无需登录

### 请求参数

**查询参数 (Query Parameters)**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Long | 否 | 1 | 当前页码 |
| size | Long | 否 | 10 | 每页大小 |
| name | String | 否 | - | 题库名称（模糊搜索） |
| tag | String | 否 | - | 标签（精确匹配） |

**请求示例 - 基础查询**:
```
GET /api/bank?current=1&size=10
```

**请求示例 - 按名称搜索**:
```
GET /api/bank?current=1&size=10&name=Java
```

**请求示例 - 按标签筛选**:
```
GET /api/bank?current=1&size=10&tag=高级
```

**请求示例 - 组合查询**:
```
GET /api/bank?current=1&size=10&name=Java&tag=高级
```

### 响应数据

**成功响应** (`code: 0`):

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "Java核心面试题",
        "description": "涵盖Java基础、集合框架、多线程、JVM等核心知识点",
        "tagList": ["Java", "基础", "并发", "数据结构"],
        "creatorId": 1,
        "status": 2,
        "reviewId": 10,
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T12:00:00"
      },
      {
        "id": 2,
        "name": "Python面试题库",
        "description": "包含Python语法、数据结构、面向对象等",
        "tagList": ["Python", "基础", "数据结构", "实战"],
        "creatorId": 1,
        "status": 2,
        "reviewId": 11,
        "createdAt": "2024-01-01T11:00:00",
        "updatedAt": "2024-01-01T13:00:00"
      }
    ],
    "total": 20,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

**分页对象字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| records | List<BankVO> | 当前页的数据列表 |
| total | Long | 总记录数 |
| size | Long | 每页大小 |
| current | Long | 当前页码 |
| pages | Long | 总页数 |

**错误响应**:

```json
{
  "code": 50000,
  "message": "系统内部异常",
  "data": null
}
```

### 注意事项

1. **无需认证**：此接口不需要登录，可以直接访问
2. **分页参数**：
   - `current`：页码从 1 开始
   - `size`：每页记录数，建议设置为 10、20、50 等
3. **名称搜索**：使用模糊匹配（LIKE），支持部分匹配
4. **标签筛选**：使用精确匹配，匹配题库标签列表中包含该标签的记录
5. **组合查询**：可以同时使用 `name` 和 `tag` 参数，两个条件是 AND 关系
6. **排序规则**：按创建时间倒序排列（最新的在前）
7. **标签格式**：返回的 `tagList` 是数组格式

---

## 题库状态说明

| 状态值 | 说明 | 描述 |
|--------|------|------|
| 0 | 草稿 | 保存为草稿，未提交审核 |
| 1 | 待审 | 已提交审核，等待审核 |
| 2 | 通过 | 审核通过，可以正常使用 |
| 3 | 驳回 | 审核被驳回，需要修改后重新提交 |

---

## 标签说明

### 可用标签（共15个）

1. Java
2. Python
3. JavaScript
4. 算法
5. 数据结构
6. 数据库
7. 前端
8. 后端
9. 框架
10. 系统设计
11. 网络
12. 并发
13. 基础
14. 高级
15. 实战

### 标签使用规则

- 每个题库可以使用多个标签（建议2-4个）
- 不同题库可以共用相同的标签
- 标签筛选时使用精确匹配

---

## 完整请求示例

### 使用 Axios

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://your-server-domain/api',
  timeout: 10000
});

// 请求拦截器：自动添加Token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 1. 创建题库（保存草稿）
const createBankDraft = async () => {
  const response = await api.post('/bank', {
    name: 'Java核心面试题',
    description: '涵盖Java基础、集合框架、多线程、JVM等核心知识点',
    tagList: ['Java', '基础', '并发', '数据结构'],
    submitForReview: false
  });
  console.log(response.data);
};

// 2. 创建题库（提交审核）
const createBankSubmit = async () => {
  const response = await api.post('/bank', {
    name: 'Java核心面试题',
    description: '涵盖Java基础、集合框架、多线程、JVM等核心知识点',
    tagList: ['Java', '基础', '并发', '数据结构'],
    submitForReview: true
  });
  console.log(response.data);
};

// 3. 更新题库
const updateBank = async (id) => {
  const response = await api.put(`/bank/${id}`, {
    name: 'Java核心面试题（更新）',
    description: '更新后的描述',
    tagList: ['Java', '高级', '实战'],
    submitForReview: true
  });
  console.log(response.data);
};

// 4. 查询题库详情
const getBankById = async (id) => {
  const response = await api.get(`/bank/${id}`);
  console.log(response.data);
};

// 5. 分页查询题库列表
const getBankList = async (current = 1, size = 10) => {
  const response = await api.get('/bank', {
    params: { current, size }
  });
  console.log(response.data);
};

// 6. 按名称搜索
const searchByName = async (name) => {
  const response = await api.get('/bank', {
    params: { current: 1, size: 10, name }
  });
  console.log(response.data);
};

// 7. 按标签筛选
const filterByTag = async (tag) => {
  const response = await api.get('/bank', {
    params: { current: 1, size: 10, tag }
  });
  console.log(response.data);
};

// 8. 组合查询
const searchCombined = async (name, tag) => {
  const response = await api.get('/bank', {
    params: { current: 1, size: 10, name, tag }
  });
  console.log(response.data);
};

// 9. 查询所有标签
const getAllTags = async () => {
  const response = await api.get('/bank/tags');
  console.log(response.data);
  // 返回: ["后端", "高级", "基础", "Java", ...]
};
```

### 使用 Fetch

```javascript
// 创建题库
const createBank = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://your-server-domain/api/bank', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      name: 'Java核心面试题',
      description: '涵盖Java基础、集合框架、多线程、JVM等核心知识点',
      tagList: ['Java', '基础', '并发', '数据结构'],
      submitForReview: false
    })
  });
  const data = await response.json();
  console.log(data);
};

// 查询题库列表
const getBankList = async () => {
  const response = await fetch('http://your-server-domain/api/bank?current=1&size=10');
  const data = await response.json();
  console.log(data);
};
```

---

## 通用错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40101 | 未登录或无权限 |
| 40300 | 禁止访问（需要管理员权限） |
| 40400 | 请求数据不存在 |
| 50000 | 系统内部异常 |
| 50001 | 操作失败 |

---

## 注意事项总结

1. **权限要求**：
   - 创建和更新题库需要管理员（ADMIN）权限
   - 查询接口无需登录

2. **Token携带**：
   - 需要认证的接口必须在请求头中携带Token
   - 格式：`Authorization: Bearer <token>`

3. **提交模式**：
   - `submitForReview = false`：保存草稿（状态=0）
   - `submitForReview = true`：提交审核（状态=1）

4. **标签格式**：
   - 请求时：`tagList` 是数组格式 `["tag1", "tag2"]`
   - 响应时：`tagList` 也是数组格式

5. **查询功能**：
   - 支持按名称模糊搜索
   - 支持按标签精确筛选
   - 支持组合查询

6. **状态管理**：
   - 任何状态的题库都可以更新
   - 更新后状态会根据 `submitForReview` 重新设置

