# PlantPal — Tree Management and Care Tracking System

ระบบจัดการและติดตามดูแลต้นไม้ เป็นเว็บแอปที่ช่วยจัดการข้อมูลต้นไม้
และติดตามการดูแลต้นไม้แต่ละต้นอย่างเป็นระบบ เช่น การรดน้ำ
การใส่ปุ๋ย การเปลี่ยนกระถาง ประวัติการดูแล และการรายงานสุขภาพต้นไม้ให้ผู้ดูแลระบบช่วยแนะนำ

🌐 **เว็บที่ deploy แล้ว:** https://plantpal-owy8.onrender.com
(Render free tier หลับเมื่อไม่มีคนใช้ เปิดครั้งแรกอาจช้าเกือบ 1 นาที)

## Members

| ชื่อ | รหัสนักศึกษา | Branch | ส่วนที่รับผิดชอบ |
|---|---|---|---|
| นางสาวกมลพร เกตุแก้ว | 673380571-1 | `Kamolpon_6733805711_03` | รายงานสุขภาพต้นไม้, การแจ้งเตือน, งานตั้งเวลา (Observer, Template Method) |
| นางสาวพรีมภัทร ภาวัฒนวคุณ | 673380594-9 | `preemphat_6733805949_03` | สมัครสมาชิก / เข้าสู่ระบบ, จัดการผู้ใช้ (Command, Chain of Responsibility) |
| นางสาวมุกดา บุญประจันทร์ | 673380598-1 | `Mukda_6733805981_03` | จัดการต้นไม้, สถานะสุขภาพ, REST API (State, Memento) |
| นางสาวสรนันท์ บุสดี | 673380605-0 | `Soranan_6733806050_03` | ตารางการดูแล, ปฏิทิน, ประวัติการดูแล (Strategy, Iterator) |

## Objectives
เพื่อพัฒนาระบบที่ช่วยจัดการข้อมูลและติดตามการดูแลต้นไม้
ให้สะดวก เป็นระบบ และช่วยลดความยุ่งยากในการบันทึกข้อมูล

## Features

**ผู้ใช้ (USER)**
- สมัครสมาชิก / เข้าสู่ระบบ / แก้ไขโปรไฟล์และรหัสผ่าน
- แดชบอร์ดสรุปจำนวนต้นไม้ตามสถานะสุขภาพ งานดูแลที่ใกล้ถึงกำหนด และรายงานที่รอผล
- เพิ่ม แก้ไข ลบ และดูรายละเอียดต้นไม้ พร้อมย้อนการแก้ไขล่าสุดได้
- เปลี่ยนสถานะสุขภาพต้นไม้ (ปกติ / ป่วย / กำลังฟื้นตัว / ตาย) ตามเส้นทางที่อนุญาต
- ตารางการดูแลสร้างให้อัตโนมัติเมื่อเพิ่มต้นไม้ (รดน้ำ / ใส่ปุ๋ย / เปลี่ยนกระถาง) คำนวณรอบจากพันธุ์ไม้
- หน้าการดูแลแบ่งงานเป็น เลยกำหนด / วันนี้ / ถัดไป และกด "ทำแล้ว" เพื่อบันทึกและเลื่อนไปรอบถัดไป
- ปฏิทินการดูแล 30 วันข้างหน้า
- ประวัติการดูแล บอกว่าทำตรงเวลาหรือช้า
- ส่งรายงานสุขภาพต้นไม้พร้อมรูป แจ้งว่าอาการดีขึ้น หรือส่งรายงานติดตามผลรอบใหม่
- การแจ้งเตือนในระบบ (แอดมินตอบรายงาน, งานดูแลพรุ่งนี้, งานเลยกำหนด)

**ผู้ดูแลระบบ (ADMIN)**
- ตอบคำแนะนำหรือปฏิเสธรายงานสุขภาพ
- จัดการพันธุ์ไม้และรอบการดูแลของแต่ละพันธุ์
- จัดการผู้ใช้ (เปลี่ยนบทบาท / ระงับบัญชี) และย้อนคำสั่งล่าสุดได้

**ระบบอัตโนมัติ (ทุกวันตามเวลาไทย)**
- 08:00 แจ้งเตือนงานดูแลที่ครบกำหนดพรุ่งนี้
- 08:05 แจ้งเตือนงานดูแลที่เลยกำหนด
- 08:10 ปิดรายงานที่ไม่มีการอัปเดตผลเกิน 14 วัน

**REST API** (`/api/v1/plants`, `/api/v1/reports`) พร้อมเอกสาร Swagger UI ที่ `/swagger-ui.html`

## Tech Stack

| ส่วน | เทคโนโลยี |
|---|---|
| ภาษา / Framework | Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation) |
| หน้าเว็บ | Thymeleaf, HTML, CSS, JavaScript |
| ฐานข้อมูล | PostgreSQL (Neon) + Flyway migration |
| เก็บรูปภาพ | Cloudinary |
| เอกสาร API | springdoc-openapi (Swagger UI) |
| ทดสอบ | JUnit 5, Mockito, AssertJ |
| Build / Deploy | Maven, Docker, GitHub Actions (CI), Render |

## Design Patterns

| กลุ่ม | Pattern |
|---|---|
| Architectural | Layered Architecture, MVC, Repository, Service Layer, DTO + Mapper, Dependency Injection |
| GoF Behavioral | Strategy, State, Observer, Command, Chain of Responsibility, Memento, Iterator, Template Method |

รายละเอียดปัญหาที่แก้ เหตุผลที่เลือก และ Class Diagram ของแต่ละ Pattern อยู่ที่ [doc/design-patterns.md](doc/design-patterns.md)

## โครงสร้างโปรเจกต์

```
Tree_Management_Final/
├── code/                      # โปรเจกต์ Spring Boot
│   ├── src/main/java/com/example/plantpal/
│   │   ├── controller/        # web (หน้า Thymeleaf) และ api (REST)
│   │   ├── service/           # business logic + service/strategy (Strategy)
│   │   ├── repository/        # Spring Data JPA
│   │   ├── domain/            # entity และ enum
│   │   ├── dto/, mapper/      # รับ-ส่งข้อมูลระหว่างชั้น
│   │   ├── event/             # Observer
│   │   ├── plant/state/       # State
│   │   ├── plant/memento/     # Memento
│   │   ├── command/           # Command
│   │   ├── validation/        # Chain of Responsibility
│   │   ├── iterator/          # Iterator
│   │   └── job/               # Template Method (งานตั้งเวลา)
│   ├── src/main/resources/    # templates, static, db/migration
│   ├── src/test/              # unit test
│   ├── Dockerfile
│   └── docker-compose.yml
├── doc/                       # เอกสารและ diagram
└── test/                      # Test Report
```

## วิธีรันโปรเจกต์

### แบบที่ 1: Docker (แนะนำ)
ต้องมี [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```bash
cd code
cp .env.example .env      # ใส่ CLOUDINARY_URL ถ้าต้องการอัปโหลดรูป (ไม่ใส่ก็เปิดได้)
docker compose up --build
```

เปิด http://localhost:8080 ได้แอปพร้อม PostgreSQL ของตัวเอง Flyway สร้างตารางและข้อมูลตัวอย่างให้ตอนเปิดครั้งแรก

### แบบที่ 2: รันด้วย Maven
ต้องมี Java 21 และ PostgreSQL

```powershell
cd code
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/plantpal"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:CLOUDINARY_URL=""     # ใส่ค่าจริงถ้าต้องการอัปโหลดรูป
.\mvnw spring-boot:run
```
(macOS / Linux ใช้ `export ชื่อ=ค่า` และ `./mvnw spring-boot:run`)

## การทดสอบ

```powershell
cd code
.\mvnw test
```

ผลล่าสุด: **111 test case ผ่านทั้งหมด** (Failures 0, Errors 0)
`PlantpalApplicationTests` ต้องต่อฐานข้อมูลจริง ส่วนเทสต์อื่นใช้ Mockito จำลอง Repository
GitHub Actions รันเทสต์ทุกครั้งที่ push หรือเปิด PR เข้า `develop` / `main` แล้ว deploy ขึ้น Render อัตโนมัติเมื่อ push เข้า `develop` และเทสต์ผ่าน

รายละเอียด Test Case ทั้งหมดอยู่ที่ [test/TEST_REPORT.md](test/TEST_REPORT.md)

## เอกสาร

| เอกสาร | ไฟล์ |
|---|---|
| Use Case Diagram และคำอธิบาย | [doc/use-case.md](doc/use-case.md) |
| Activity Diagram | [doc/activity-diagram.md](doc/activity-diagram.md) |
| Sequence Diagrams | [doc/sequence-diagrams.md](doc/sequence-diagrams.md) |
| Domain Model | [doc/domain-model.md](doc/domain-model.md) |
| ER Diagram และ Data Dictionary | [doc/data-dictionary.md](doc/data-dictionary.md) |
| Component Diagram | [doc/component-diagram.md](doc/component-diagram.md) |
| Deployment Diagram | [doc/deployment-diagram.md](doc/deployment-diagram.md) |
| Design Patterns | [doc/design-patterns.md](doc/design-patterns.md) |
| SOLID Analysis | [doc/solid-analysis.md](doc/solid-analysis.md) |
| Test Report | [test/TEST_REPORT.md](test/TEST_REPORT.md) |