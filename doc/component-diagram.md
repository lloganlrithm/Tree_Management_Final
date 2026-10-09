# Component Diagram — PlantPal

ส่วนประกอบของระบบและการเรียกกันระหว่างชั้น ตั้งแต่ผู้ใช้เข้ามาผ่าน Spring Security → Controller → Service → Repository → ฐานข้อมูล รวมถึงงานเบื้องหลังและระบบภายนอก

![Component Diagram](diagrams/component-diagram.png)

## ส่วนประกอบหลัก

| ส่วนประกอบ | หน้าที่ | อ้างอิงในโค้ด |
|---|---|---|
| Spring Security | ทุก request ผ่านตรงนี้ก่อน ตรวจ login และแยกสิทธิ์ USER / ADMIN | `config/SecurityConfig` |
| Controller (หน้าเว็บ) | รับ request จากหน้าเว็บ แล้วส่ง Model ให้ Thymeleaf แสดงผล | `controller/web/`, `resources/templates/` |
| Controller (REST API) | API แบบ JSON สำหรับต้นไม้และรายงานสุขภาพ ดูได้ใน Swagger | `controller/api/PlantApiController`, `HealthReportApiController` |
| ตัวจัดการ Error กลาง | error จาก REST API ทุกตัวตอบเป็น JSON รูปแบบเดียวกัน | `exception/GlobalExceptionHandler` |
| Service | business logic ทั้งหมด Controller ไม่เรียก Repository ตรง | `service/`, `service/impl/` |
| งานเบื้องหลัง: Listener | รับ event จาก Service / job แล้วสร้างแจ้งเตือน (Observer) | `event/NotificationListener` |
| งานเบื้องหลัง: Job รายวัน | รันเองทุกเช้า เตือนงานดูแลและปิดรายงานที่ค้างนาน (Template Method) | `job/AbstractDailyJob` และคลาสลูก |
| Repository | อ่าน / เขียนฐานข้อมูลผ่าน Spring Data JPA | `repository/` |
| PostgreSQL | ฐานข้อมูล 8 ตาราง สร้างและอัปเดตด้วย Flyway | `resources/db/migration/` |
| Cloudinary | เก็บรูปรายงานสุขภาพและรูปโปรไฟล์ DB เก็บแค่ลิงก์ | `service/impl/CloudinaryImageStorageService` |

## สีในแผนภาพ

| สี | โมดูล |
|---|---|
| ชมพู | ผู้ใช้ / Security |
| ส้ม | ต้นไม้ |
| เขียว | การดูแล |
| แดง | รายงานสุขภาพ |
| เหลือง | แจ้งเตือน (Observer) |
| ม่วง | Job รายวัน (Template Method) |
| ฟ้า | Controller |
| เขียวอมฟ้า | Repository, ที่เก็บรูป |
| เทา | ระบบภายนอก (เบราว์เซอร์, Swagger, PostgreSQL, Cloudinary) |
