-- เพิ่มรอบใส่ปุ๋ยและเปลี่ยนกระถางของแต่ละสายพันธุ์ (NULL = ใช้ค่าตั้งต้นใน Strategy)
ALTER TABLE species
    ADD COLUMN fertilize_interval_days INTEGER CHECK (fertilize_interval_days > 0),
    ADD COLUMN repot_interval_days     INTEGER CHECK (repot_interval_days > 0);