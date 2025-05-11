# 自习室预约系统 API 文档 - 用户相关接口

## 目录
- [通用规范](#通用规范)
- [用户相关接口](#用户相关接口)
  - [用户登录](#1-用户登录)
  - [用户注册](#2-用户注册)
  - [获取黑名单用户列表](#3-获取黑名单用户列表)
  - [移除黑名单用户](#4-移除黑名单用户)
  - [将用户添加到黑名单](#5-将用户添加到黑名单)
  - [用户认证](#用户认证)
  - [用户个人信息](#用户个人信息)

## 通用规范

### 基础URL
```
http://your-api-domain.com/api
```

### 请求头
- 需要身份验证的接口必须携带 `Authorization` 头
```
Authorization: Bearer <JWT_TOKEN>
```

### 响应格式
所有接口均返回 JSON 格式数据，标准响应结构如下：
```json
{
  "code": 200,          // 状态码
  "message": "success", // 状态消息
  "data": {}            // 响应数据
}
```

### 错误响应
当发生错误时，返回相应的 HTTP 状态码和错误信息：
```json
{
  "code": 400,                  // HTTP 状态码
  "message": "错误信息描述",      // 错误详情
  "data": null                  // 通常为空
}
```

## 用户相关接口

### 1. 用户登录

- **接口描述**: 用户登录并获取认证令牌
- **请求方式**: `POST`
- **接口地址**: `/users/login`
- **权限要求**: 无需认证

**请求参数**:
```json
{
  "username": "string", // 用户名，必填
  "password": "string"  // 密码，必填
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "JWT令牌",
    "user": {
      "id": "用户ID",
      "username": "用户名"
    }
  }
}
```

**错误响应**:
- `400 Bad Request`: 用户名或密码不正确
- `500 Internal Server Error`: 服务器内部错误

### 2. 用户注册

- **接口描述**: 注册新用户
- **请求方式**: `POST`
- **接口地址**: `/users/register`
- **权限要求**: 无需认证

**请求参数**:
```json
{
  "username": "string", // 用户名，必填
  "password": "string"  // 密码，必填
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": "用户ID",
    "username": "用户名"
  }
}
```

**错误响应**:
- `400 Bad Request`: 用户名已存在或格式不正确
- `500 Internal Server Error`: 服务器内部错误

### 3. 获取黑名单用户列表

- **接口描述**: 获取所有被加入黑名单的用户列表
- **请求方式**: `GET`
- **接口地址**: `/users/blacklist`
- **权限要求**: 管理员权限

**请求参数**: 无

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": "user123",
      "username": "张三",
      "blacklistTime": "2023-04-01T10:30:00",
      "remainingTime": 86400000,  // 剩余时间（毫秒）
      "reason": "多次未签到"
    }
  ]
}
```

**空数据响应** (200):
```json
{
  "code": 200,
  "message": "当前黑名单中没有成员",
  "data": []
}
```

**错误响应**:
- `403 Forbidden`: 无权限访问
- `500 Internal Server Error`: 服务器内部错误

### 4. 移除黑名单用户

- **接口描述**: 将指定用户从黑名单中移除
- **请求方式**: `DELETE`
- **接口地址**: `/users/blacklist/{userId}`
- **权限要求**: 管理员权限

**路径参数**:
- `userId`: 用户ID，必填

**请求参数**: 无

**成功响应** (200):
```json
{
  "code": 200,
  "message": "用户已从黑名单中移除",
  "data": null
}
```

**错误响应**:
- `403 Forbidden`: 无权限访问
- `404 Not Found`: 用户不存在
- `500 Internal Server Error`: 服务器内部错误

### 5. 将用户添加到黑名单

- **接口描述**: 将指定用户添加到黑名单
- **请求方式**: `POST`
- **接口地址**: `/users/blacklist/{userId}`
- **权限要求**: 管理员权限

**路径参数**:
- `userId`: 用户ID，必填

**请求参数**: 无

**成功响应** (200):
```json
{
  "code": 200,
  "message": "用户已添加到黑名单，期限2天",
  "data": {
    "userId": "user123",
    "username": "张三",
    "blacklistTime": "2023-04-01T10:30:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 用户已在黑名单中
- `403 Forbidden`: 无权限访问
- `404 Not Found`: 用户不存在
- `500 Internal Server Error`: 服务器内部错误

## 用户认证

### 用户注册
- **接口**: `POST /api/users/register`
- **请求体**:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "id": "用户ID",
      "username": "用户名"
    }
  }
  ```

### 用户登录
- **接口**: `POST /api/users/login`
- **请求体**:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "JWT令牌",
      "user": {
        "id": "用户ID",
        "username": "用户名"
      }
    }
  }
  ```

## 用户个人信息

### 获取用户信息
- **接口**: `GET /api/users/{id}`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": "用户ID",
      "username": "用户名",
      "createdAt": "创建时间"
    }
  }
  ```

### 修改用户密码
- **接口**: `PUT /api/users/change-password`
- **请求体**:
  ```json
  {
    "userId": "用户ID",
    "oldPassword": "旧密码",
    "newPassword": "新密码"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": null
  }
  ```

## 注意事项

1. 所有需要身份验证的接口都需要在请求头中携带有效的JWT令牌
2. 密码长度必须在8-20位之间
3. 用户名必须是唯一的
4. 修改密码时需要验证旧密码 