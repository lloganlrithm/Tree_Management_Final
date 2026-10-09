# SOLID Analysis — PlantPal

## SOLID คืออะไร

| ตัวอักษร | ชื่อเต็ม | ความหมาย | ในโปรเจคเราเห็นจาก |
|---|---|---|---|
| **S** | Single Responsibility Principle | 1 class ควรมีหน้าที่เดียว มีเหตุผลเดียวที่ต้องแก้ | service ทำ business, validator ตรวจข้อมูล, repository บันทึก แยกกันคนละ class |
| **O** | Open/Closed Principle | เปิดให้เพิ่มความสามารถ แต่ปิดไม่ให้ต้องแก้โค้ดเดิม | เพิ่ม validator / command / strategy / ที่เก็บรูปใหม่ ด้วยการสร้าง class ใหม่ ไม่ต้องแก้ if-else เดิม |
| **L** | Liskov Substitution Principle | ใช้ class ลูกแทน class แม่ (หรือ interface) ได้โดยโปรแกรมยังทำงานถูก | validator ทุกตัว, command ทุกตัว, ที่เก็บรูปทั้งสองแบบ สลับกันใช้ได้ ไม่มีตัวไหนโยน `UnsupportedOperationException` |
| **I** | Interface Segregation Principle | แยก interface ให้เล็กตามการใช้งาน ไม่บังคับให้ใครต้องรู้จัก method ที่ไม่ได้ใช้ | `UserService`, `ProfileService`, `AdminUserService`, `CurrentUserService` แยกตามหน้าที่เรียกใช้ |
| **D** | Dependency Inversion Principle | class ควรขึ้นกับ interface (abstraction) ไม่ใช่ class จริง และรับ dependency ผ่าน constructor | service / controller ทุกตัวรับ interface ผ่าน constructor (`@RequiredArgsConstructor` + `final`) ไม่มี `@Autowired` |

รายละเอียดแต่ละข้อพร้อมไฟล์
---

## S : Single Responsibility Principle

> แต่ละ class มีหน้าที่เดียว ไม่รวม Business + Validation + Persistence ไว้ใน class เดียว

| ไฟล์  | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `service/impl/UserServiceImpl.java`  | `register()` เรียก `registerValidator.validate(request)`  แล้วสร้าง User + UserProfile แล้ว `userRepository.save()`  | service ทำแค่ business ของการสมัคร การตรวจข้อมูลแยกไปอยู่ใน `validation/` ส่วนการบันทึกเป็นหน้าที่ของ repository |
| `validation/EmailFormatValidator.java`  | `check()` ตรวจแค่รูปแบบอีเมล | validator แต่ละตัวตรวจเรื่องเดียว (รูปแบบอีเมล / อีเมลซ้ำ / ความยาวรหัส / รหัสตรงกัน) แยกเป็น 4 class |
| `validation/RegisterValidationChain.java`  | `@Bean` ที่ต่อ validator 4 ตัวเป็นสาย | การ "ประกอบลำดับการตรวจ" แยกออกมาเป็น class ของตัวเอง ไม่ปนกับ service หรือ validator |
| `command/UserCommandInvoker.java`  | รัน command, เก็บประวัติ, undo | ทำหน้าที่เดียวคือจัดการประวัติคำสั่ง ไม่รู้ว่าคำสั่งข้างในทำอะไร |
| `exception/GlobalExceptionHandler.java`  | `@RestControllerAdvice` รวมการแปลง exception เป็น `ErrorResponse` | controller ไม่ต้อง try-catch เอง หน้าที่ "ตอบ error" อยู่ที่เดียว |
| `service/impl/CurrentUserServiceImpl.java`  | `getCurrentUser()` ดึงผู้ใช้ที่ login อยู่จาก `SecurityContextHolder` | งานอ่าน session ของ Spring Security แยกมาไว้ที่นี่ service อื่นไม่ต้องยุ่งกับ security เอง |
| `controller/api/PlantApiController.java` | รับ request แล้วเรียก `plantService` ส่งผลผ่าน `mapper.toResponse()` | controller ทำแค่เรื่อง HTTP (status code, Location) business อยู่ใน service |
| `mapper/PlantMapper.java` | `toResponse()` แปลง `Plant` เป็น `PlantResponse` | งานแปลง entity เป็น DTO แยกมาที่เดียว controller กับ service ไม่ต้องทำเอง |
| `plant/memento/PlantEditHistory.java` | `save()`, `canUndo()`, `take()` เก็บ/คืน `PlantSnapshot` | Caretaker เก็บ snapshot อย่างเดียว ไม่รู้ว่าข้างในมีอะไร การสร้าง/คืนค่าอยู่ที่ `PlantServiceImpl` |
| `event/NotificationListener.java` | ฟัง event 4 แบบ แล้วเรียก `notificationService.create(...)` อย่างเดียว | หน้าที่เดียวคือ "แปลง event เป็นแจ้งเตือน" ไม่มี business ของรายงานหรือการดูแลปนอยู่ |
| `mapper/HealthReportMapper.java` | `toResponse(HealthReport)` | แปลง entity เป็น DTO อย่างเดียว ไม่ยุ่งกับการบันทึกหรือ validation |
| `job/AbstractDailyJob.java` | `run()` จัดการ transaction, วันที่ (เวลาไทย), log ไว้ที่เดียว | งานส่วนกลางของ job รายวันอยู่ในคลาสแม่ คลาสลูกเขียนแค่ "หาอะไร / ทำอะไร" |

---

## O : Open/Closed Principle

> เพิ่มฟีเจอร์ใหม่ด้วยการเพิ่ม class ไม่ใช่ไปแก้ if-else เดิม

| ไฟล์  | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `validation/RegisterValidator.java`  | abstract class ที่มี `protected abstract void check(...)` | จะเพิ่มเงื่อนไขการสมัครใหม่ เช่น ห้ามใช้อีเมลบางโดเมน แค่สร้าง class ใหม่ที่ `extends RegisterValidator` แล้วต่อใน chain ไม่ต้องแก้ validator เดิมหรือ `UserServiceImpl` |
| `command/UserCommand.java`  | interface `execute()` / `undo()` / `description()` | จะเพิ่มคำสั่งแอดมินใหม่ เช่น รีเซ็ตรหัสผ่าน แค่สร้าง class ใหม่ที่ `implements UserCommand` invoker ใช้ได้ทันทีโดยไม่ต้องแก้ |
| `service/impl/CloudinaryAvatarStorageService.java`  | `implements AvatarStorageService` ใช้ `@Primary` + `@ConditionalOnExpression` | ตอนเปลี่ยนที่เก็บรูปโปรไฟล์จากเครื่องไปใช้ Cloudinary ทำโดยเพิ่ม class ใหม่ ไม่ได้แก้ `LocalAvatarStorageService` หรือ `ProfileServiceImpl` เลย |
| `service/strategy/CareIntervalStrategy.java`  | interface ของ Strategy คำนวณรอบการดูแล | งานดูแลชนิดใหม่ = เพิ่ม strategy class ใหม่ ไม่ต้องแก้ switch/if เดิม |
| `plant/state/PlantHealthStates.java` | `register(new HealthyState())` ... ลงใน `EnumMap` | จะเพิ่มสถานะสุขภาพใหม่ แค่สร้าง class ที่ `implements PlantHealthState` แล้ว register ไม่ต้องแก้ if-else ใน `PlantServiceImpl` หรือ controller |
| `job/AbstractDailyJob.java` | `CareDueReminderJob`, `OverdueCareReminderJob`, `StaleReportCloseJob` extends คลาสนี้ | เพิ่ม job รายวันตัวใหม่ = สร้างคลาสใหม่ที่ extends แล้วเขียน `findTargets()` กับ `process()` ไม่ต้องแก้คลาสแม่หรือ job เดิม |
| `event/NotificationListener.java` | service ส่งแค่ `publishEvent(...)` ไม่รู้ว่าใครฟัง | จะเพิ่มการแจ้งเตือนแบบใหม่ เช่น ส่งอีเมล ทำได้ด้วยการเพิ่ม listener ใหม่ ฝั่ง `HealthReportServiceImpl` ที่ส่ง event ไม่ต้องแก้เลย |
---

## L : Liskov Substitution Principle

> subclass ใช้แทน superclass ได้โดยไม่ทำให้ตรรกะพัง ไม่ throw `UnsupportedOperationException`

| ไฟล์  | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `validation/EmailFormatValidator.java`, `EmailNotTakenValidator.java`, `PasswordLengthValidator.java`, `PasswordMatchValidator.java` | ทุกตัว override `check()` และถ้าไม่ผ่านจะ throw `RegistrationException` แบบเดียวกัน | `RegisterValidator.validate()`  เรียก `check()` ของตัวไหนก็ได้โดยไม่ต้องรู้ว่าเป็นตัวไหน สลับลำดับหรือเปลี่ยนตัวใน chain แล้วยังทำงานถูก |
| `command/ChangeRoleCommand.java`, `command/SetActiveCommand.java`  | implement ทั้ง `execute()` และ `undo()` จริงครบทุก method | `UserCommandInvoker.run()` / `undoLast()`  รับ `UserCommand` ตัวไหนก็ได้ ไม่มีตัวไหนที่ undo ไม่ได้หรือโยน exception ว่าไม่รองรับ |
| `service/impl/LocalAvatarStorageService.java`, `service/impl/CloudinaryAvatarStorageService.java`  | ทั้งสองตัว `implements AvatarStorageService` และ `store()` คืน URL ของรูปเหมือนกัน | `ProfileServiceImpl`  เรียก `avatarStorageService.store(avatar)` ได้เหมือนเดิมไม่ว่า Spring จะฉีดตัวไหนมา |
| `plant/state/HealthyState.java`, `SickState.java`, `RecoveringState.java`, `DeadState.java` | ทุกตัว implement `PlantHealthState` และเมื่อเปลี่ยนไม่ได้จะ throw `InvalidHealthTransitionException` แบบเดียวกัน | `PlantServiceImpl` เรียก `PlantHealthStates.of(status).changeTo(plant, target)` ได้โดยไม่ต้องรู้ว่าเป็นสถานะไหน สลับตัวไหนมาก็ทำงานถูก |
| ทั้งโปรเจค | ค้นทั้ง `code/src/main` ไม่พบ `UnsupportedOperationException` | ไม่มี implementation ไหนที่ทำไม่ได้ แล้วโยน exception แทน |
| `job/CareDueReminderJob.java`, `OverdueCareReminderJob.java`, `StaleReportCloseJob.java` | ทั้ง 3 ตัว override แค่ `name()`, `findTargets()`, `process()` ส่วน `run()` เป็น `final` | ใช้ job ตัวไหนแทน `AbstractDailyJob` ก็ได้ เรียก `run()` แล้วลำดับขั้นตอนเหมือนกันทุกตัว ไม่มีตัวไหนข้ามขั้นหรือโยน exception ว่าไม่รองรับ |
---

## I : Interface Segregation Principle

> แยก interface ย่อยตามการใช้งาน ไม่มี Fat Interface

| ไฟล์ | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `service/UserService.java`  | มีแค่ `register()` กับ `findAdminIds()` | งานเกี่ยวกับผู้ใช้แยกเป็น 4 interface ตามคนเรียก แทนที่จะรวมเป็น `UserService` ก้อนใหญ่ก้อนเดียว  หน้า register ใช้ตัวนี้ |
| `service/ProfileService.java`  | `getMyProfile()`, `updateMyProfile()`, `changeMyPassword()`, `getNavUser()` | ใช้เฉพาะหน้าโปรไฟล์ของตัวเอง |
| `service/AdminUserService.java`  | `search()`, `changeRole()`, `setActive()`, `undoLast()`, `lastCommandDescription()` | ใช้เฉพาะหน้าแอดมินจัดการผู้ใช้ ผู้ใช้ทั่วไปไม่ต้องรู้จัก method พวกนี้ |
| `service/CurrentUserService.java` | มีแค่ 3 method: `getCurrentUser()`, `getCurrentUserId()`, `isAdmin()` | service อื่นที่อยากรู้แค่ ใคร login อยู่ ขึ้นกับ interface เล็กๆ ตัวนี้ ไม่ต้องได้ method สมัครหรือแก้โปรไฟล์ติดมาด้วย |
| `plant/state/PlantHealthState.java` | มีแค่ `status()`, `canChangeTo()` และ `onLeave()` ที่เป็น `default` | สถานะที่ไม่มีงานตอนออก (`HealthyState`, `DeadState`) ไม่ต้อง override `onLeave()` ทำเฉพาะ `SickState` กับ `RecoveringState` |
| `service/AvatarStorageService.java` / `service/ImageStorageService.java` | แต่ละตัวมี method เดียว: `store(file)` และ `upload(file, folder)` | คนเรียกต้องการแค่ "อัปโหลดแล้วได้ URL" เลยรู้จักแค่ method นี้ ไม่ต้องรู้ว่าข้างหลังเก็บในเครื่องหรือ Cloudinary |
| `config/ApiSecurityErrorHandler.java` | `implements AuthenticationEntryPoint, AccessDeniedHandler` | Spring Security แยก interface 401 กับ 403 ไว้เป็นสองตัวเล็กๆ class นี้ implement เฉพาะสองตัวที่ต้องใช้ |

---

## D : Dependency Inversion Principle

> Service ขึ้นกับ interface ไม่ใช่ concrete class และใช้ Constructor Injection เท่านั้น

| ไฟล์  | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `service/impl/UserServiceImpl.java`  | `@RequiredArgsConstructor` + field `final` ของ `UserRepository`, `PasswordEncoder`, `RegisterValidator` | ใช้ constructor injection และขึ้นกับ abstraction ทั้งหมด (`RegisterValidator` เป็น abstract — ไม่รู้ว่าข้างใน chain มีตัวไหน) |
| `service/impl/ProfileServiceImpl.java`  | `CurrentUserService`, `UserRepository`, `PasswordEncoder`, `AvatarStorageService` | ขึ้นกับ interface `AvatarStorageService` ไม่ใช่ `LocalAvatarStorageService` หรือ `CloudinaryAvatarStorageService` เลยสลับที่เก็บรูปได้โดยไม่แก้ class นี้ |
| `service/impl/AdminUserServiceImpl.java`  | `UserRepository`, `CurrentUserService`, `UserCommandInvoker` | ขึ้นกับ `CurrentUserService` (interface) ไม่ใช่ `CurrentUserServiceImpl` |
| `controller/web/AuthWebController.java`  | `private final UserService userService` | controller ขึ้นกับ interface ของ service ไม่ใช่ `UserServiceImpl` |
| `controller/web/ProfileWebController.java`  | `private final ProfileService profileService` | เหมือนกัน |
| `controller/web/AdminUserWebController.java`  | `private final AdminUserService adminUserService` | เหมือนกัน |
| `controller/web/PlantController.java`, `controller/api/PlantApiController.java` | `private final PlantService plantService` | controller ทั้งหน้าเว็บและ REST API ขึ้นกับ interface `PlantService` ไม่ใช่ `PlantServiceImpl` |
| `service/impl/PlantServiceImpl.java` | `ApplicationEventPublisher eventPublisher` ส่ง `PlantCreatedEvent` หลังเพิ่มต้นไม้ | ไม่เรียก `CareServiceImpl` ตรง ๆ จึงไม่ผูกกับโมดูลการดูแล ฝั่งไหนจะฟัง event ก็ได้ |
| ทั้งโปรเจค | ค้นทั้ง `code/src/main` ไม่พบ `@Autowired` | ทุกที่ใช้ constructor injection (`@RequiredArgsConstructor` + `final`) ตามที่ใบงานกำหนด |
| `job/CareDueReminderJob.java`, `OverdueCareReminderJob.java` | `private final CareService careService` | job ขึ้นกับ interface `CareService` ไม่อ่าน `CareScheduleRepository` ของโมดูลการดูแลตรงๆ |
| `job/StaleReportCloseJob.java` | `private final HealthReportService healthReportService` | ขึ้นกับ interface ไม่ใช่ `HealthReportServiceImpl` |
| `event/NotificationListener.java` | `NotificationService`, `UserService` | หารายชื่อแอดมินผ่าน `UserService` (interface) ไม่เรียก `UserRepository` ตรงๆ |

---