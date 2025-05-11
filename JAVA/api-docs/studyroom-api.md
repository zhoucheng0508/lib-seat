# 自习室预约系统 API 文档 - 自习室相关接口

## 目录
- [自习室相关接口](#自习室相关接口)
  - [获取自习室列表](#1-获取自习室列表)
  - [获取自习室详情](#2-获取自习室详情)
  - [获取自习室座位](#3-获取自习室座位)
  - [新增座位](#4-新增座位)
  - [编辑座位](#5-编辑座位)
  - [删除座位](#6-删除座位)
  - [删除所有座位](#7-删除所有座位)

## 自习室相关接口

### 1. 获取自习室列表

- **接口描述**: 获取所有可用的自习室列表
- **请求方式**: `GET`
- **接口地址**: `/study-rooms`
- **权限要求**: 无需认证

**查询参数**:
- `status` (可选): 按状态筛选，可选值：AVAILABLE, MAINTENANCE, CLOSED
- `page` (可选): 页码，默认1
- `size` (可选): 每页记录数，默认10

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "page": 1,
    "size": 10,
    "studyRooms": [
      {
        "id": "room1",
        "name": "一号自习室",
        "location": "图书馆一楼",
        "description": "安静舒适的学习环境",
        "openTime": "08:00",
        "closeTime": "22:00",
        "status": "AVAILABLE",
        "totalSeats": 50,
        "availableSeats": 30,
        "imageUrl": "https://example.com/images/room1.jpg"
      },
      {
        "id": "room2",
        "name": "二号自习室",
        "location": "图书馆二楼",
        "description": "适合小组讨论",
        "openTime": "09:00",
        "closeTime": "21:00",
        "status": "AVAILABLE",
        "totalSeats": 30,
        "availableSeats": 15,
        "imageUrl": "https://example.com/images/room2.jpg"
      }
      // 更多自习室...
    ]
  }
}
```

**空数据响应** (200):
```json
{
  "code": 200,
  "message": "暂无可用自习室",
  "data": {
    "total": 0,
    "page": 1,
    "size": 10,
    "studyRooms": []
  }
}
```

**错误响应**:
- `500 Internal Server Error`: 服务器内部错误

### 2. 获取自习室详情

- **接口描述**: 获取指定自习室的详细信息
- **请求方式**: `GET`
- **接口地址**: `/study-rooms/{studyRoomId}`
- **权限要求**: 无需认证

**路径参数**:
- `studyRoomId`: 自习室ID，必填

**查询参数**:
- `dateStr` (可选): 按日期筛选，格式 YYYY-MM-DD，默认当天
- `startTime` (可选): 开始时间，格式 HH:mm
- `endTime` (可选): 结束时间，格式 HH:mm

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "room1",
    "name": "一号自习室",
    "location": "图书馆一楼",
    "description": "安静舒适的学习环境",
    "openTime": "08:00",
    "closeTime": "22:00",
    "status": "AVAILABLE",
    "totalSeats": 50,
    "availableSeats": 30,
    "imageUrl": "https://example.com/images/room1.jpg",
    "availabilityInfo": {
      "date": "2023-04-02",
      "timeSlots": [
        {
          "startTime": "08:00",
          "endTime": "10:00",
          "totalSeats": 50,
          "bookedSeats": 5
        },
        {
          "startTime": "10:00",
          "endTime": "12:00",
          "totalSeats": 50,
          "bookedSeats": 10
        }
        // 更多时间段...
      ]
    }
  }
}
```

**错误响应**:
- `404 Not Found`: 自习室不存在
- `500 Internal Server Error`: 服务器内部错误

### 3. 获取自习室座位

- **接口描述**: 获取指定自习室的所有座位
- **请求方式**: `GET`
- **接口地址**: `/study-rooms/{roomId}/seats`
- **权限要求**: 无需认证（普通用户）或管理员权限（管理功能）

**路径参数**:
- `roomId`: 自习室ID，必填

**查询参数**:
- `date` (可选): 按日期筛选，格式 YYYY-MM-DD，默认当天
- `startTime` (可选): 开始时间，格式 HH:mm
- `endTime` (可选): 结束时间，格式 HH:mm
- `status` (可选): 按状态筛选，可选值：AVAILABLE, BOOKED, DISABLED

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studyRoomId": "room1",
    "studyRoomName": "一号自习室",
    "date": "2023-04-02",
    "timeRange": {
      "startTime": "14:00",
      "endTime": "16:00"
    },
    "seats": [
      {
        "id": "seat1",
        "seatNumber": "A1",
        "status": "AVAILABLE",
        "position": {
          "x": 0,
          "y": 0
        },
        "type": "REGULAR",
        "features": ["POWER_OUTLET"]
      },
      {
        "id": "seat2",
        "seatNumber": "A2",
        "status": "BOOKED",
        "position": {
          "x": 1,
          "y": 0
        },
        "type": "REGULAR",
        "features": ["POWER_OUTLET", "DESK_LAMP"]
      }
      // 更多座位...
    ],
    "layout": {
      "rows": 5,
      "columns": 10,
      "walls": [
        { "x1": 2, "y1": 0, "x2": 2, "y2": 5 }
      ],
      "doors": [
        { "x": 0, "y": 0 }
      ]
    }
  }
}
```

**空数据响应** (200):
```json
{
  "code": 200,
  "message": "该自习室暂无座位",
  "data": {
    "studyRoomId": "room1",
    "studyRoomName": "一号自习室",
    "seats": []
  }
}
```

**错误响应**:
- `404 Not Found`: 自习室不存在
- `500 Internal Server Error`: 服务器内部错误

### 4. 新增座位

- **接口描述**: 在指定自习室中添加新座位
- **请求方式**: `POST`
- **接口地址**: `/study-rooms/{roomId}/seats`
- **权限要求**: 管理员权限

**路径参数**:
- `roomId`: 自习室ID，必填

**请求参数**:
```json
{
  "seatNumber": "C5",                    // 座位编号，必填
  "position": {                          // 座位位置，必填
    "x": 2,
    "y": 4
  },
  "type": "REGULAR",                     // 座位类型，可选：REGULAR, DISABLED, VIP
  "features": ["POWER_OUTLET", "DESK_LAMP"] // 座位特性，可选
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "座位添加成功",
  "data": {
    "id": "seat15",
    "seatNumber": "C5",
    "status": "AVAILABLE",
    "position": {
      "x": 2,
      "y": 4
    },
    "type": "REGULAR",
    "features": ["POWER_OUTLET", "DESK_LAMP"],
    "createdAt": "2023-04-02T10:15:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 参数错误或座位编号已存在
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 自习室不存在
- `500 Internal Server Error`: 服务器内部错误

### 5. 编辑座位

- **接口描述**: 修改指定自习室中的座位信息
- **请求方式**: `PUT`
- **接口地址**: `/study-rooms/{roomId}/seats/{seatId}`
- **权限要求**: 管理员权限

**路径参数**:
- `roomId`: 自习室ID，必填
- `seatId`: 座位ID，必填

**请求参数**:
```json
{
  "seatNumber": "C6",                    // 座位编号，可选
  "position": {                          // 座位位置，可选
    "x": 2,
    "y": 5
  },
  "type": "VIP",                        // 座位类型，可选
  "features": ["POWER_OUTLET", "DESK_LAMP", "PRIVACY_SCREEN"] // 座位特性，可选
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "座位信息已更新",
  "data": {
    "id": "seat15",
    "seatNumber": "C6",
    "status": "AVAILABLE",
    "position": {
      "x": 2,
      "y": 5
    },
    "type": "VIP",
    "features": ["POWER_OUTLET", "DESK_LAMP", "PRIVACY_SCREEN"],
    "updatedAt": "2023-04-02T11:30:00"
  }
}
```

**错误响应**:
- `400 Bad Request`: 参数错误或座位编号已存在
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 自习室或座位不存在
- `500 Internal Server Error`: 服务器内部错误

### 6. 删除座位

- **接口描述**: 删除指定自习室中的座位
- **请求方式**: `DELETE`
- **接口地址**: `/study-rooms/{roomId}/seats/{seatId}`
- **权限要求**: 管理员权限

**路径参数**:
- `roomId`: 自习室ID，必填
- `seatId`: 座位ID，必填

**成功响应** (200):
```json
{
  "code": 200,
  "message": "座位已删除",
  "data": {
    "id": "seat15",
    "deletedAt": "2023-04-02T14:20:00"
  }
}
```

**错误响应**:
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 自习室或座位不存在
- `409 Conflict`: 座位当前有预约，无法删除
- `500 Internal Server Error`: 服务器内部错误

### 7. 删除所有座位

- **接口描述**: 删除指定自习室中的所有座位
- **请求方式**: `DELETE`
- **接口地址**: `/study-rooms/{roomId}/seats`
- **权限要求**: 管理员权限

**路径参数**:
- `roomId`: 自习室ID，必填

**成功响应** (200):
```json
{
  "code": 200,
  "message": "所有座位已删除",
  "data": {
    "studyRoomId": "room1",
    "totalDeleted": 30,
    "deletedAt": "2023-04-02T15:45:00"
  }
}
```

**错误响应**:
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 自习室不存在
- `409 Conflict`: 部分座位当前有预约，无法删除
- `500 Internal Server Error`: 服务器内部错误 