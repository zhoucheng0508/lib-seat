# 自习室预约系统 API 文档 - 预约相关接口

## 目录
- [预约相关接口](#预约相关接口)
  - [创建预约](#1-创建预约)
  - [获取用户预约列表](#2-获取用户预约列表)
  - [预约签到](#3-预约签到)
  - [取消预约](#4-取消预约)

## 预约相关接口

### 1. 创建预约

- **接口描述**: 创建一个新的自习室座位预约
- **请求方式**: `POST`
- **接口地址**: `/reservations`
- **权限要求**: 用户登录

**请求参数**:
```json
{
  "studyRoomId": "room1",      // 自习室ID，必填
  "seatId": "seat5",           // 座位ID，必填
  "date": "2023-04-05",        // 预约日期，必填，格式 YYYY-MM-DD
  "startTime": "14:00",        // 开始时间，必填，格式 HH:mm
  "endTime": "16:00"           // 结束时间，必填，格式 HH:mm
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "预约成功",
  "data": {
    "id": "res789",
    "userId": "user123",
    "studyRoomId": "room1",
    "studyRoomName": "一号自习室",
    "seatId": "seat5",
    "seatNumber": "A5",
    "status": "PENDING",
    "date": "2023-04-05",
    "startTime": "14:00",
    "endTime": "16:00",
    "createdAt": "2023-04-02T10:30:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 参数错误或该座位已被预约
- `401 Unauthorized`: 用户未登录
- `403 Forbidden`: 用户在黑名单中
- `404 Not Found`: 自习室或座位不存在
- `409 Conflict`: 用户在同一时段已有其他预约
- `500 Internal Server Error`: 服务器内部错误

### 2. 获取用户预约列表

- **接口描述**: 获取指定用户的预约列表
- **请求方式**: `GET`
- **接口地址**: `/reservations/user/{userId}`
- **权限要求**: 用户登录（只能查看自己的，管理员可查看任意用户）

**路径参数**:
- `userId`: 用户ID，必填

**查询参数**:
- `status` (可选): 预约状态，可选值：PENDING, ACTIVE, COMPLETED, CANCELLED
- `date` (可选): 按日期筛选，格式 YYYY-MM-DD
- `page` (可选): 页码，默认1
- `size` (可选): 每页记录数，默认10

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "reservations": [
      {
        "id": "res789",
        "studyRoomId": "room1",
        "studyRoomName": "一号自习室",
        "seatId": "seat5",
        "seatNumber": "A5",
        "status": "PENDING",
        "date": "2023-04-05",
        "startTime": "14:00",
        "endTime": "16:00",
        "createdAt": "2023-04-02T10:30:00"
      },
      {
        "id": "res456",
        "studyRoomId": "room2",
        "studyRoomName": "二号自习室",
        "seatId": "seat3",
        "seatNumber": "B3",
        "status": "ACTIVE",
        "date": "2023-04-03",
        "startTime": "09:00",
        "endTime": "11:00",
        "checkinTime": "2023-04-03T09:05:30",
        "createdAt": "2023-04-01T16:45:00"
      }
      // 更多预约记录...
    ]
  }
}
```

**空数据响应** (200):
```json
{
  "code": 200,
  "message": "暂无预约记录",
  "data": {
    "total": 0,
    "page": 1,
    "size": 10,
    "reservations": []
  }
}
```

**错误响应**:
- `401 Unauthorized`: 用户未登录
- `403 Forbidden`: 无权限查看该用户预约
- `404 Not Found`: 用户不存在
- `500 Internal Server Error`: 服务器内部错误

### 3. 预约签到

- **接口描述**: 用户到达自习室后进行预约签到
- **请求方式**: `POST`
- **接口地址**: `/reservations/{id}/check-in`
- **权限要求**: 用户登录

**路径参数**:
- `id`: 预约ID，必填

**请求参数**: 无

**成功响应** (200):
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "id": "res789",
    "status": "ACTIVE",
    "checkinTime": "2023-04-05T14:03:20"
  }
}
```

**错误响应**:
- `400 Bad Request`: 预约状态不是PENDING（已取消或已签到）
- `401 Unauthorized`: 用户未登录
- `403 Forbidden`: 不是自己的预约
- `404 Not Found`: 预约不存在
- `409 Conflict`: 非签到时间（提前15分钟或迟到15分钟以上）
- `500 Internal Server Error`: 服务器内部错误

### 4. 取消预约

- **接口描述**: 取消一个未使用的预约
- **请求方式**: `PUT`
- **接口地址**: `/reservations/{id}/cancel`
- **权限要求**: 用户登录

**路径参数**:
- `id`: 预约ID，必填

**请求参数**:
```json
{
  "reason": "临时有事，无法前往" // 取消原因，可选
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "预约已取消",
  "data": {
    "id": "res789",
    "status": "CANCELLED",
    "cancelReason": "临时有事，无法前往",
    "cancelledAt": "2023-04-04T19:30:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 预约状态不是PENDING（已取消或已签到）
- `401 Unauthorized`: 用户未登录
- `403 Forbidden`: 不是自己的预约
- `404 Not Found`: 预约不存在
- `409 Conflict`: 取消时间太晚（距离预约开始时间小于2小时）
- `500 Internal Server Error`: 服务器内部错误 