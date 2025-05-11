-- 检查是否已经存在模拟数据
SET @has_mock_data = (SELECT COUNT(*) FROM users WHERE username LIKE 'user%');

-- 只有当没有模拟数据时才执行插入
INSERT IGNORE INTO `users` (`id`, `created_at`, `password`, `username`, `blacklist_start_time`, `is_blacklisted`, `no_show_count`)
SELECT 
    UUID(),
    UNIX_TIMESTAMP() * 1000,
    '$2a$10$cpQEKIP4QM65T5EmYt2NjuLkUF9L9kkLszfSuWkKGSZH0995xcUDu',
    CONCAT('user', n),
    NULL,
    0,
    FLOOR(RAND() * 4)
FROM (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
) numbers
WHERE @has_mock_data = 0;

-- 只有当没有模拟数据时才更新黑名单
UPDATE `users` 
SET `blacklist_start_time` = NOW(),
    `is_blacklisted` = b'1'
WHERE `username` IN ('user1', 'user2', 'user3', 'user4', 'user5')
AND @has_mock_data = 0;

-- 只有当没有模拟数据时才生成预约记录
INSERT IGNORE INTO `reservations` 
(`id`, `created_at`, `date`, `end_time`, `remarks`, `seat_id`, `start_time`, `status`, `study_room_id`, `updated_at`, `user_id`, `adjusted_at`, `adjusted_by`, `deleted_at`, `deleted_by`, `is_deleted`)
SELECT 
    UUID(),
    UNIX_TIMESTAMP() * 1000,
    DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 7) DAY),
    -- 结束时间：确保比开始时间晚2-4小时
    TIME_FORMAT(
        ADDTIME(
            start_time, 
            SEC_TO_TIME((FLOOR(RAND() * 3) + 2) * 3600)
        ), 
        '%H:00:00'
    ),
    CONCAT('自习预约-', FLOOR(RAND() * 1000)),
    (SELECT `id` FROM `seats` ORDER BY RAND() LIMIT 1),
    -- 开始时间：8:00-20:00之间的整点
    TIME_FORMAT(
        ADDTIME(
            '08:00:00', 
            SEC_TO_TIME(FLOOR(RAND() * 12) * 3600)
        ), 
        '%H:00:00'
    ),
    IF(RAND() > 0.5, 'CONFIRMED', 'CHECKED_IN'),
    (SELECT `id` FROM `study_rooms` ORDER BY RAND() LIMIT 1),
    UNIX_TIMESTAMP() * 1000,
    (SELECT `id` FROM `users` WHERE `username` LIKE 'user%' AND `username` != 'user13' ORDER BY RAND() LIMIT 1),
    NULL,
    NULL,
    NULL,
    NULL,
    0
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS numbers
    CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) AS users
WHERE @has_mock_data = 0;

-- 为user13生成特殊的预约记录
INSERT IGNORE INTO `reservations` 
(`id`, `created_at`, `date`, `end_time`, `remarks`, `seat_id`, `start_time`, `status`, `study_room_id`, `updated_at`, `user_id`, `adjusted_at`, `adjusted_by`, `deleted_at`, `deleted_by`, `is_deleted`)
SELECT 
    UUID(),
    UNIX_TIMESTAMP() * 1000,
    DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 7) DAY),
    TIME_FORMAT(
        ADDTIME(
            '10:00:00', 
            SEC_TO_TIME((FLOOR(RAND() * 3) + 2) * 3600)
        ), 
        '%H:00:00'
    ),
    '自习预约-特殊',
    (SELECT `id` FROM `seats` ORDER BY RAND() LIMIT 1),
    '10:00:00',
    'CONFIRMED',
    (SELECT `id` FROM `study_rooms` ORDER BY RAND() LIMIT 1),
    UNIX_TIMESTAMP() * 1000,
    (SELECT `id` FROM `users` WHERE `username` = 'user13'),
    NULL,
    NULL,
    NULL,
    NULL,
    0
WHERE @has_mock_data = 0;