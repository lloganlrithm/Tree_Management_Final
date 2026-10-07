-- บัญชีทดสอบ (รหัสผ่านเก็บเป็น BCrypt hash)
--   admin@plantpal.com / admin1234  (ADMIN)
--   user@plantpal.com  / user1234   (USER)
-- ต้องเปลี่ยนรหัสผ่านก่อน deploy จริง เพราะ repo เป็น public

INSERT INTO users (email, password, role) VALUES
    ('admin@plantpal.com', '$2a$10$bJU.LUFKEj2Xe8xuyUMirOWGtjt.PjEaWyJbXjs44hUNIyQlg3fMm', 'ADMIN'),
    ('user@plantpal.com',  '$2a$10$9uhcRENr/hSABRSn.ToY9.4FN8Cgv66Eb4Qd6rW8yMiBHXgl1GOzm', 'USER');

INSERT INTO user_profiles (user_id, first_name, last_name)
SELECT id, 'แอดมิน', 'ทดสอบ' FROM users WHERE email = 'admin@plantpal.com';

INSERT INTO user_profiles (user_id, first_name, last_name)
SELECT id, 'ผู้ใช้', 'ทดสอบ' FROM users WHERE email = 'user@plantpal.com';
