-- ข้อมูลตัวอย่างสำหรับ Demo (ต้องรันหลัง V3 ที่สร้าง user@plantpal.com)
-- เขียนให้รันซ้ำบน DB ที่มีข้อมูลอยู่แล้วได้ (เช่น Neon): ชื่อซ้ำจะถูกข้าม ไม่ทับของเดิม
-- วันที่อิงจาก CURRENT_DATE เพื่อให้มีงานดูแล "เลยกำหนด / วันนี้ / พรุ่งนี้" โชว์ใน Dashboard
-- ห้ามแก้ไฟล์นี้หลังรันแล้ว ถ้าจะเพิ่มข้อมูล ให้สร้าง V6 ใหม่

-- 1) พันธุ์ไม้ (name ซ้ำ = ข้าม)
INSERT INTO species (name, water_interval_days, sunlight_requirement, description,
                     fertilize_interval_days, repot_interval_days)
VALUES
    ('มอนสเตอร่า',  7,  'MEDIUM', 'ใบใหญ่มีรู ชอบแสงรำไร ดินชื้นแต่ไม่แฉะ',     30, 365),
    ('ลิ้นมังกร',   14, 'LOW',    'ทนแล้ง ฟอกอากาศ อยู่ในที่แสงน้อยได้',        60, 730),
    ('กระบองเพชร',  21, 'HIGH',   'ชอบแดดจัด รดน้ำน้อย ระวังรากเน่า',            90, 730),
    ('พลูด่าง',     5,  'MEDIUM', 'โตเร็ว เลื้อยได้ ชอบความชื้น',                30, 365),
    ('ยางอินเดีย',  7,  'MEDIUM', 'ใบมันหนา เช็ดใบบ่อยๆ ให้สังเคราะห์แสงได้ดี', 30, 365),
    ('เฟิร์นบอสตัน', 3, 'LOW',    'ชอบความชื้นสูง ห้ามให้ดินแห้ง',              30, 365)
ON CONFLICT (name) DO NOTHING;

-- 2) ต้นไม้ของ user@plantpal.com หลายสถานะ (ชื่อเล่นซ้ำของคนเดิม = ข้าม)
INSERT INTO plants (user_id, species_id, nickname, health_status, recovery_count, planted_date)
SELECT u.id, s.id, v.nickname, v.status, v.recovery, CURRENT_DATE - v.age_days
FROM (VALUES
        ('เขียวขจี',  'มอนสเตอร่า', 'HEALTHY',    1, 120),
        ('น้องลิ้น',  'ลิ้นมังกร',  'HEALTHY',    0, 200),
        ('หนามน้อย', 'กระบองเพชร', 'HEALTHY',    0, 60),
        ('ใบด่าง',   'พลูด่าง',    'SICK',       0, 45),
        ('ยางยืด',   'ยางอินเดีย', 'RECOVERING', 0, 90)
     ) AS v(nickname, species_name, status, recovery, age_days)
JOIN users u   ON u.email = 'user@plantpal.com'
JOIN species s ON s.name = v.species_name
WHERE NOT EXISTS (SELECT 1 FROM plants p WHERE p.user_id = u.id AND p.nickname = v.nickname);

-- 3) ตารางดูแล (UNIQUE plant_id + action_type ซ้ำ = ข้าม)
--    offset ติดลบ = เลยกำหนด, 0 = วันนี้, 1 = พรุ่งนี้
INSERT INTO care_schedules (plant_id, action_type, next_due_date)
SELECT p.id, v.action_type, CURRENT_DATE + v.offset_days
FROM (VALUES
        ('เขียวขจี',  'WATER',     -1),
        ('เขียวขจี',  'FERTILIZE', 12),
        ('เขียวขจี',  'REPOT',     200),
        ('น้องลิ้น',  'WATER',     0),
        ('น้องลิ้น',  'FERTILIZE', 40),
        ('หนามน้อย', 'WATER',     1),
        ('หนามน้อย', 'REPOT',     300),
        ('ใบด่าง',   'WATER',     0),
        ('ใบด่าง',   'FERTILIZE', -2),
        ('ยางยืด',   'WATER',     3),
        ('ยางยืด',   'FERTILIZE', 20)
     ) AS v(nickname, action_type, offset_days)
JOIN users u  ON u.email = 'user@plantpal.com'
JOIN plants p ON p.user_id = u.id AND p.nickname = v.nickname
ON CONFLICT (plant_id, action_type) DO NOTHING;

-- 4) ประวัติการดูแลย้อนหลัง (ใส่เฉพาะต้นที่ยังไม่มีประวัติ)
INSERT INTO care_logs (plant_id, care_schedule_id, action_type, due_date, performed_at, notes)
SELECT p.id, c.id, v.action_type, CURRENT_DATE - v.due_ago, NOW() - (v.done_ago || ' days')::INTERVAL, v.notes
FROM (VALUES
        ('เขียวขจี', 'WATER', 8,  8,  'รดน้ำตามรอบ'),
        ('เขียวขจี', 'WATER', 15, 14, 'รดช้าไป 1 วัน'),
        ('น้องลิ้น', 'WATER', 14, 14, NULL)
     ) AS v(nickname, action_type, due_ago, done_ago, notes)
JOIN users u  ON u.email = 'user@plantpal.com'
JOIN plants p ON p.user_id = u.id AND p.nickname = v.nickname
LEFT JOIN care_schedules c ON c.plant_id = p.id AND c.action_type = v.action_type
WHERE NOT EXISTS (SELECT 1 FROM care_logs l WHERE l.plant_id = p.id);

-- 5) รายงานสุขภาพ: รอแอดมินตอบ 1 + ตอบแล้ว 1 (หัวข้อซ้ำของต้นเดิม = ข้าม)
INSERT INTO health_reports (plant_id, title, description, severity, status, admin_reply,
                            created_at, resolved_at)
SELECT p.id, v.title, v.description, v.severity, v.status, v.admin_reply,
       NOW() - (v.created_ago || ' days')::INTERVAL,
       CASE WHEN v.resolved_ago IS NULL THEN NULL
            ELSE NOW() - (v.resolved_ago || ' days')::INTERVAL END
FROM (VALUES
        ('ใบด่าง', 'ใบเหลืองและมีจุดสีน้ำตาล', 'ใบล่างเหลือง 3 ใบ มีจุดน้ำตาลที่ขอบใบ', 'MEDIUM',
         'PENDING', NULL, 1, NULL),
        ('ยางยืด', 'ใบร่วงหลังย้ายที่', 'ย้ายมาไว้ข้างหน้าต่าง ใบร่วงวันละ 1-2 ใบ', 'LOW',
         'RESOLVED', 'น่าจะปรับตัวกับแสงใหม่ ไม่ต้องย้ายอีก รดน้ำตามรอบเดิม 1-2 สัปดาห์จะดีขึ้น', 6, 4)
     ) AS v(nickname, title, description, severity, status, admin_reply, created_ago, resolved_ago)
JOIN users u  ON u.email = 'user@plantpal.com'
JOIN plants p ON p.user_id = u.id AND p.nickname = v.nickname
WHERE NOT EXISTS (SELECT 1 FROM health_reports r WHERE r.plant_id = p.id AND r.title = v.title);