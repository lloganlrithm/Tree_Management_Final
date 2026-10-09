# Sequence Diagrams — PlantPal

แสดงลำดับการเรียกกันระหว่าง class ในแต่ละ scenario หลักของระบบ

| # | Scenario | ผู้รับผิดชอบ | Pattern ที่เห็น |
|---|---|---|---|
| 1 | สมัครสมาชิก | Preemphat | Chain of Responsibility |
| 2 | เข้าสู่ระบบ | Preemphat | — (Spring Security) |
| 3 | เพิ่มต้นไม้ | (เพื่อนเติม) | (เพื่อนเติม) |
| 4 | แอดมินตอบรายงานสุขภาพ | (เพื่อนเติม) | (เพื่อนเติม) |

## 1. สมัครสมาชิก

ผู้ใช้ส่งฟอร์มสมัคร → `AuthWebController` ตรวจ `@Valid` → `UserServiceImpl` ส่งต่อให้ validator 4 ตัวตรวจทีละขั้น (ไม่ผ่านตัวไหนหยุดทันที) → ผ่านครบจึง hash รหัสผ่านและบันทึก User + UserProfile → กลับไปหน้า login

![Register](diagrams/sequence-register-diagram.png)

## 2. เข้าสู่ระบบ

Spring Security หาผู้ใช้จากอีเมล → เช็คว่าบัญชีถูกระงับไหม (ก่อนตรวจรหัสผ่าน) → ตรวจรหัสผ่านด้วย BCrypt → ผ่านแล้วแยกหน้าตาม role (ADMIN → `/admin/reports`, USER → `/dashboard`)

![Login](diagrams/sequence-login-diagram.png)

## 3. เพิ่มต้นไม้

![Add Plant](diagrams/sequence-add-plant-diagram.png)

## 4. แอดมินตอบรายงานสุขภาพ

![Report Reply](diagrams/sequence-report-reply-diagram.png)