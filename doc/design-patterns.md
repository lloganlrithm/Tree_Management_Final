# Design Patterns — PlantPal

## สรุปภาพรวม

| Pattern | กลุ่ม | ปัญหาที่แก้ | ไฟล์/คลาสที่ใช้ | ผู้รับผิดชอบ |
|---|---|---|---|---|
| Layered Architecture | Architectural |  | `controller/` → `service/` → `repository/` → `domain/` |  |
| MVC | Architectural |  | `controller/web/` + `templates/` |  |
| Repository | Architectural |  | `repository/` |  |
| Service Layer | Architectural |  | `service/`, `service/impl/` |  |
| DTO + Mapper | Architectural |  | `dto/request/`, `dto/response/`, `mapper/` |  |
| Dependency Injection | Architectural |  | Constructor Injection ทุก service/controller |  |
| Strategy | GoF Behavioral |  | `service/strategy/` |  |
| State | GoF Behavioral |  | `plant/state/` |  |
| Observer | GoF Behavioral |  | `event/` |  |
| Command | GoF Behavioral |  | `command/` | Preemphat |
| Chain of Responsibility | GoF Behavioral |  | `validation/` | Preemphat |
| Memento | GoF Behavioral |  | `plant/memento/` |  |
| Iterator | GoF Behavioral |  | `iterator/` |  |

---

## 1. Enterprise / Architectural Patterns (บังคับทุกกลุ่ม)

### 1.1 Layered Architecture
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** `controller/` → `service/` → `repository/` → `domain/` (ห้าม controller เรียก repository ตรง)
- **เหตุผลที่เลือก:** _(รอใส่)_

### 1.2 MVC
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** Controller = `controller/web/*Controller`, View = `resources/templates/`, Model = DTO ที่ส่งเข้า `Model`
- **เหตุผลที่เลือก:** _(รอใส่)_

### 1.3 Repository
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** `repository/*Repository` (Spring Data JPA)
- **เหตุผลที่เลือก:** _(รอใส่)_

### 1.4 Service Layer
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** interface ใน `service/` + implementation ใน `service/impl/`
- **เหตุผลที่เลือก:** _(รอใส่)_

### 1.5 DTO + Mapper
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** `dto/request/`, `dto/response/`, `mapper/PlantMapper`, `mapper/HealthReportMapper`
- **เหตุผลที่เลือก:** _(รอใส่)_

### 1.6 Dependency Injection
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** Constructor Injection (`@RequiredArgsConstructor` + field `final`) ทุก service/controller
- **เหตุผลที่เลือก:** _(รอใส่)_

---

## 2. GoF Patterns — กลุ่ม Behavioral

### 2.1 Strategy
- **ผู้รับผิดชอบ:** _(รอใส่)_
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:**
  - `service/strategy/CareIntervalStrategy` (interface)
  - `service/strategy/WaterIntervalStrategy`, `FertilizeIntervalStrategy`, `RepotIntervalStrategy`
  - `service/strategy/CareIntervalCalculator` (เลือก strategy ตาม `ActionType`)
- **เหตุผลที่เลือก:** _(รอใส่)_
- **Class Diagram:** _(รอใส่)_

### 2.2 State
- **ผู้รับผิดชอบ:** _(รอใส่)_
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:**
  - `plant/state/PlantHealthState` (interface)
  - `plant/state/HealthyState`, `SickState`, `RecoveringState`, `DeadState`
  - `plant/state/PlantHealthStates`, `plant/state/InvalidHealthTransitionException`
- **เหตุผลที่เลือก:** _(รอใส่)_
- **Class Diagram:** _(รอใส่)_

### 2.3 Observer
- **ผู้รับผิดชอบ:** _(รอใส่)_
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:**
  - Event: `event/CareDueEvent`, `PlantCreatedEvent`, `ReportSubmittedEvent`, `ReportResolvedEvent`, `ReportAutoClosedEvent`
  - Listener: `event/NotificationListener`
  - ผู้ส่ง event: (service/job ไหนเรียก `publishEvent`) _(รอใส่)_
- **เหตุผลที่เลือก:** _(รอใส่)_
- **Class Diagram:** _(รอใส่)_

### 2.4 Command
- **ผู้รับผิดชอบ:** Preem
- **ปัญหาที่แก้:** 
  - หน้าจัดการผู้ใช้ แอดมินเปลี่ยน role หรือกดระงับบัญชีได้ทันทีด้วยคลิกเดียว ถ้ากดผิดคนต้องไปหาเองว่าเดิมเป็นค่าอะไรแล้วแก้กลับเอง
  - ถ้าเขียนแบบปกติ service แก้ค่าแล้ว save เลย จะไม่มีที่เก็บค่าเดิมไว้ย้อนกลับ
- **ไฟล์/คลาสที่ใช้:**
  - `command/UserCommand` (interface)
  - `command/ChangeRoleCommand`
  - `command/SetActiveCommand`
  - `command/UserCommandInvoker`
  - `service/impl/AdminUserServiceImpl`
- **เหตุผลที่เลือก:** 
  - เราเปลี่ยน การกระทำ ให้เป็น object แต่ละคำสั่งเลยจำได้เองว่าก่อนทำค่าเป็นอะไร พอกดย้อนก็แค่เรียก `undo()` ค่าก็กลับมาเหมือนเดิม
  - Invoker ใช้ `@SessionScope` แอดมินแต่ละคนมีประวัติของตัวเอง ไม่ปนกัน
  - จะเพิ่มคำสั่งใหม่ เช่น รีเซ็ตรหัสผ่าน แค่สร้างคลาสใหม่ที่ implements `UserCommand` ไม่ต้องแก้ invoker หรือคำสั่งเดิม 
- **Class Diagram:**
- ![Command](diagrams/pattern-command.png)

### 2.5 Chain of Responsibility
- **ผู้รับผิดชอบ:** Preemphat
- **ปัญหาที่แก้:** 
  - ตอนสมัครสมาชิกต้องตรวจหลายเรื่อง รูปแบบอีเมล, อีเมลซ้ำ, ความยาวรหัสผ่าน, รหัสผ่านตรงกัน
  - ถ้าเขียนรวมใน `UserServiceImpl.register()` จะเป็น if-else ยาว service ทำหลายหน้าที่ ผิด SRP และเพิ่มเงื่อนไขใหม่ทีไรต้องแก้ service ทุกครั้ง
- **ไฟล์/คลาสที่ใช้:**
  - `validation/RegisterValidator` (abstract class มี `next` ชี้ไปตัวถัดไป)
  - `validation/EmailFormatValidator`, `EmailNotTakenValidator`, `PasswordLengthValidator`, `PasswordMatchValidator`
  - `validation/RegisterValidationChain`
  - `service/impl/UserServiceImpl`
- **เหตุผลที่เลือก:** 
  - แต่ละ validator ตรวจแค่เรื่องเดียว ไม่ผ่านก็โยน `RegistrationException` แล้ว chain หยุดทันที ผู้ใช้เห็น error ทีละข้อตามลำดับ
  - เรียงจากเช็คง่ายไปยาก รูปแบบอีเมลก่อน ค่อยไปถาม DB ว่าซ้ำไหม ถ้าอีเมลผิดรูปแบบก็ไม่ต้องเสียเวลาถาม DB
  - จะเพิ่มหรือสลับลำดับการตรวจ แก้ที่ `RegisterValidationChain` ที่เดียว ไม่ต้องแตะ `UserServiceImpl`
  - เขียน unit test แยกแต่ละตัวได้ (`RegisterValidationChainTest`)
- **Class Diagram:**
![Chain of Responsibility](diagrams/pattern-chain-of-responsibility.png)

### 2.6 Memento
- **ผู้รับผิดชอบ:** _(รอใส่)_
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** `plant/memento/PlantSnapshot`, `plant/memento/PlantEditHistory`
- **เหตุผลที่เลือก:** _(รอใส่)_
- **Class Diagram:** _(รอใส่)_

### 2.7 Iterator
- **ผู้รับผิดชอบ:** _(รอใส่)_
- **ปัญหาที่แก้:** _(รอใส่)_
- **ไฟล์/คลาสที่ใช้:** `iterator/CareCalendarIterator` (implements `Iterator<CareCalendarDay>`)
- **เหตุผลที่เลือก:** _(รอใส่)_
- **Class Diagram:** _(รอใส่)_
