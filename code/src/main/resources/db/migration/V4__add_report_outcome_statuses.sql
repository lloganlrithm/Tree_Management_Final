-- เพิ่มสถานะที่บอกว่ารายงานรอบนั้นจบแบบไหน (เดิมทุกแบบเป็น RESOLVED เหมือนกันหมด)
--   FOLLOWED_UP = ผู้ใช้กด "ยังไม่ดีขึ้น" รอบนี้ส่งต่อไปรายงานติดตามผลรอบใหม่
--   AUTO_CLOSED = ไม่มีการบอกผลเกิน 14 วัน job จบรายงานให้อัตโนมัติ
ALTER TABLE health_reports DROP CONSTRAINT health_reports_status_check;
ALTER TABLE health_reports ADD CONSTRAINT health_reports_status_check
    CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'FOLLOWED_UP', 'AUTO_CLOSED'));

-- แก้ข้อมูลเก่า: รอบที่กด "ยังไม่ดีขึ้น" ไปแล้วถูกบันทึกเป็น RESOLVED
-- หาจากรายงาน "ติดตามผล: ..." ของต้นเดียวกันที่ถูกสร้างตอนรอบนั้นจบ (บันทึกใน transaction เดียวกัน ห่างกันไม่กี่วินาที)
UPDATE health_reports r
SET status = 'FOLLOWED_UP'
WHERE r.status = 'RESOLVED'
  AND r.resolved_at IS NOT NULL
  AND EXISTS (SELECT 1
              FROM health_reports n
              WHERE n.plant_id = r.plant_id
                AND n.id <> r.id
                AND n.title LIKE 'ติดตามผล: %'
                AND n.created_at BETWEEN r.resolved_at - INTERVAL '1 minute' AND r.resolved_at + INTERVAL '1 minute');
