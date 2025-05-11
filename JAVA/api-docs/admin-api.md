# 自习室预约系统 API 文档 - 管理员相关接口

## 目录
- [管理员相关接口](#管理员相关接口)
  - [管理员登录](#1-管理员登录)
  - [获取管理员预约列表](#2-获取管理员预约列表)
  - [删除预约记录](#3-删除预约记录)
  - [调整预约状态](#4-调整预约状态)

## 管理员相关接口

### 1. 管理员登录

- **接口描述**: 管理员账号登录并获取认证令牌
- **请求方式**: `POST`
- **接口地址**: `/admins/login`
- **权限要求**: 无需认证

**请求参数**:
```json
{
  "username": "admin",    // 管理员用户名，必填
  "password": "password"  // 密码，必填
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "adminId": "admin1",
    "username": "admin",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "permissions": ["ROOM_MANAGE", "USER_MANAGE", "RESERVATION_MANAGE"],
    "createdAt": "2023-01-01T12:00:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 用户名或密码不正确
- `500 Internal Server Error`: 服务器内部错误

### 2. 获取管理员预约列表

- **接口描述**: 管理员查看所有预约记录
- **请求方式**: `GET`
- **接口地址**: `/admin/reservations`
- **权限要求**: 管理员权限

**查询参数**:
- `page` (可选): 页码，默认1
- `size` (可选): 每页记录数，默认20
- `sort` (可选): 排序字段，例如 "createdAt,desc"
- `userId` (可选): 按用户ID筛选
- `status` (可选): 按状态筛选，可选值：PENDING, ACTIVE, COMPLETED, CANCELLED
- `studyRoomId` (可选): 按自习室ID筛选
- `seatId` (可选): 按座位ID筛选
- `startDate` (可选): 开始日期，格式 YYYY-MM-DD
- `endDate` (可选): 结束日期，格式 YYYY-MM-DD

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 150,
    "page": 1,
    "size": 20,
    "reservations": [
      {
        "id": "res123",
        "userId": "user123",
        "username": "张三",
        "studyRoomId": "room1",
        "studyRoomName": "一号自习室",
        "seatId": "seat1",
        "seatNumber": "A1",
        "status": "ACTIVE",
        "date": "2023-04-01",
        "startTime": "14:00",
        "endTime": "16:00",
        "checkinTime": "2023-04-01T14:05:30",
        "createdAt": "2023-03-30T10:15:00"
      },
      // 更多预约记录...
    ]
  }
}
```

**空数据响应** (200):
```json
{
  "code": 200,
  "message": "无符合条件的预约记录",
  "data": {
    "total": 0,
    "page": 1,
    "size": 20,
    "reservations": []
  }
}
```

**错误响应**:
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `500 Internal Server Error`: 服务器内部错误

### 3. 删除预约记录

- **接口描述**: 管理员删除预约记录
- **请求方式**: `DELETE`
- **接口地址**: `/admin/reservations/{id}`
- **权限要求**: 管理员权限

**路径参数**:
- `id`: 预约ID，必填

**成功响应** (200):
```json
{
  "code": 200,
  "message": "预约记录已删除",
  "data": {
    "id": "res123",
    "deletedAt": "2023-04-02T11:30:00"
  }
}
```

**错误响应**:
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 预约不存在
- `500 Internal Server Error`: 服务器内部错误

### 4. 调整预约状态

- **接口描述**: 管理员手动调整预约状态
- **请求方式**: `PUT`
- **接口地址**: `/admin/reservations/{id}/status`
- **权限要求**: 管理员权限

**路径参数**:
- `id`: 预约ID，必填

**请求参数**:
```json
{
  "status": "COMPLETED",          // 新状态，必填
  "reason": "管理员手动完成预约"   // 说明原因，必填
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "预约状态已更新",
  "data": {
    "id": "res123",
    "previousStatus": "ACTIVE",
    "currentStatus": "COMPLETED",
    "updatedBy": "admin1",
    "updateReason": "管理员手动完成预约",
    "updatedAt": "2023-04-02T16:45:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 状态参数错误
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 预约不存在
- `500 Internal Server Error`: 服务器内部错误

## 管理员认证

### 管理员登录
- **接口**: `POST /api/admins/login`
- **请求体**:
  ```json
  {
    "username": "管理员用户名",
    "password": "管理员密码"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "JWT令牌",
      "admin": {
        "id": "管理员ID",
        "username": "管理员用户名"
      }
    }
  }
  ```

### 管理员注册
- **接口**: `POST /api/admins/register`
- **请求体**:
  ```json
  {
    "username": "管理员用户名",
    "password": "管理员密码"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "id": "管理员ID",
      "username": "管理员用户名"
    }
  }
  ```

## 管理员个人信息

### 获取管理员信息
- **接口**: `GET /api/admins/{id}`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": "管理员ID",
      "username": "管理员用户名"
    }
  }
  ```

### 修改管理员密码
- **接口**: `PUT /api/admins/change-password`
- **请求体**:
  ```json
  {
    "adminId": "管理员ID",
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

## 用户管理

### 获取所有用户信息
- **接口**: `GET /api/admins/users`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "users": [
        {
          "id": "用户ID",
          "username": "用户名",
          "createdAt": "创建时间"
        }
      ],
      "total": 用户总数
    }
  }
  ```

### 修改用户密码
- **接口**: `PUT /api/admins/users/change-password`
- **请求体**:
  ```json
  {
    "userId": "用户ID",
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

## 黑名单管理

### 获取黑名单用户列表
- **接口**: `GET /api/users/blacklist`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "users": [
        {
          "id": "用户ID",
          "username": "用户名",
          "blacklistEndTime": "黑名单结束时间"
        }
      ]
    }
  }
  ```

### 添加用户到黑名单
- **接口**: `POST /api/users/blacklist/{userId}`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "用户已添加到黑名单",
    "data": null
  }
  ```

### 从黑名单移除用户
- **接口**: `DELETE /api/users/blacklist/{userId}`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "用户已从黑名单移除",
    "data": null
  }
  ```

## 注意事项

1. 所有管理员接口都需要管理员权限（ROLE_ADMIN）
2. 密码修改接口的新密码长度必须在8-20位之间
3. 黑名单管理接口默认黑名单期限为2天
4. 所有接口都需要在请求头中携带有效的JWT令牌 