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

## S — Single Responsibility Principle

> แต่ละ class มีหน้าที่เดียว ไม่รวม Business + Validation + Persistence ไว้ใน class เดียว

| ไฟล์ (บรรทัด) | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `service/impl/UserServiceImpl.java` — บรรทัด 27–48 | `register()` เรียก `registerValidator.validate(request)` (บรรทัด 32) แล้วสร้าง User + UserProfile แล้ว `userRepository.save()` (บรรทัด 47) | service ทำแค่ business ของการสมัคร การตรวจข้อมูลแยกไปอยู่ใน `validation/` ส่วนการบันทึกเป็นหน้าที่ของ repository |
| `validation/EmailFormatValidator.java` — บรรทัด 13 | `check()` ตรวจแค่รูปแบบอีเมล | validator แต่ละตัวตรวจเรื่องเดียว (รูปแบบอีเมล / อีเมลซ้ำ / ความยาวรหัส / รหัสตรงกัน) แยกเป็น 4 class |
| `validation/RegisterValidationChain.java` — บรรทัด 37–44 | `@Bean` ที่ต่อ validator 4 ตัวเป็นสาย | การ "ประกอบลำดับการตรวจ" แยกออกมาเป็น class ของตัวเอง ไม่ปนกับ service หรือ validator |
| `command/UserCommandInvoker.java` — บรรทัด 26–52 | รัน command, เก็บประวัติ, undo | ทำหน้าที่เดียวคือจัดการประวัติคำสั่ง ไม่รู้ว่าคำสั่งข้างในทำอะไร |
| `exception/GlobalExceptionHandler.java` — บรรทัด 24–25 | `@RestControllerAdvice` รวมการแปลง exception เป็น `ErrorResponse` | controller ไม่ต้อง try-catch เอง หน้าที่ "ตอบ error" อยู่ที่เดียว |
| `service/impl/CurrentUserServiceImpl.java` — บรรทัด 17–31 | `getCurrentUser()` ดึงผู้ใช้ที่ login อยู่จาก `SecurityContextHolder` | งานอ่าน session ของ Spring Security แยกมาไว้ที่นี่ service อื่นไม่ต้องยุ่งกับ security เอง |

---

## O — Open/Closed Principle

> เพิ่มฟีเจอร์ใหม่ด้วยการเพิ่ม class ไม่ใช่ไปแก้ if-else เดิม

| ไฟล์ (บรรทัด) | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `validation/RegisterValidator.java` — บรรทัด 7, 24 | abstract class ที่มี `protected abstract void check(...)` | จะเพิ่มเงื่อนไขการสมัครใหม่ (เช่น ห้ามใช้อีเมลบางโดเมน) แค่สร้าง class ใหม่ที่ `extends RegisterValidator` แล้วต่อใน chain ไม่ต้องแก้ validator เดิมหรือ `UserServiceImpl` |
| `command/UserCommand.java` — บรรทัด 5–13 | interface `execute()` / `undo()` / `description()` | จะเพิ่มคำสั่งแอดมินใหม่ (เช่น รีเซ็ตรหัสผ่าน) แค่สร้าง class ใหม่ที่ `implements UserCommand` invoker ใช้ได้ทันทีโดยไม่ต้องแก้ |
| `service/impl/CloudinaryAvatarStorageService.java` — บรรทัด 12, 19 | `implements AvatarStorageService` ใช้ `@Primary` + `@ConditionalOnExpression` | ตอนเปลี่ยนที่เก็บรูปโปรไฟล์จากเครื่องไปใช้ Cloudinary ทำโดยเพิ่ม class ใหม่ ไม่ได้แก้ `LocalAvatarStorageService` หรือ `ProfileServiceImpl` เลย |
| `service/strategy/CareIntervalStrategy.java` — บรรทัด 9 | interface ของ Strategy คำนวณรอบการดูแล | งานดูแลชนิดใหม่ = เพิ่ม strategy class ใหม่ ไม่ต้องแก้ switch/if เดิม |

---

## L — Liskov Substitution Principle

> subclass ใช้แทน superclass ได้โดยไม่ทำให้ตรรกะพัง ไม่ throw `UnsupportedOperationException`

| ไฟล์ (บรรทัด) | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `validation/EmailFormatValidator.java`, `EmailNotTakenValidator.java`, `PasswordLengthValidator.java`, `PasswordMatchValidator.java` — บรรทัด 13, 16, 15, 9 | ทุกตัว override `check()` และถ้าไม่ผ่านจะ throw `RegistrationException` แบบเดียวกัน | `RegisterValidator.validate()` (บรรทัด 17–22) เรียก `check()` ของตัวไหนก็ได้โดยไม่ต้องรู้ว่าเป็นตัวไหน สลับลำดับหรือเปลี่ยนตัวใน chain แล้วยังทำงานถูก |
| `command/ChangeRoleCommand.java`, `command/SetActiveCommand.java` — บรรทัด 23, 32 / 22, 31 | implement ทั้ง `execute()` และ `undo()` จริงครบทุก method | `UserCommandInvoker.run()` / `undoLast()` (บรรทัด 32–47) รับ `UserCommand` ตัวไหนก็ได้ ไม่มีตัวไหนที่ undo ไม่ได้หรือโยน exception ว่าไม่รองรับ |
| `service/impl/LocalAvatarStorageService.java`, `service/impl/CloudinaryAvatarStorageService.java` — บรรทัด 17 / 19 | ทั้งสองตัว `implements AvatarStorageService` และ `store()` คืน URL ของรูปเหมือนกัน | `ProfileServiceImpl` บรรทัด 64 เรียก `avatarStorageService.store(avatar)` ได้เหมือนเดิมไม่ว่า Spring จะฉีดตัวไหนมา |
| ทั้งโปรเจค | ค้นทั้ง `code/src/main` ไม่พบ `UnsupportedOperationException` | ไม่มี implementation ไหนที่ "ทำไม่ได้" แล้วโยน exception แทน |

---

## I — Interface Segregation Principle

> แยก interface ย่อยตามการใช้งาน ไม่มี Fat Interface

| ไฟล์ (บรรทัด) | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|
| `service/UserService.java` — บรรทัด 8–15 | มีแค่ `register()` กับ `findAdminIds()` | งานเกี่ยวกับผู้ใช้แยกเป็น 4 interface ตามคนเรียก แทนที่จะรวมเป็น `UserService` ก้อนใหญ่ก้อนเดียว — หน้า register ใช้ตัวนี้ |
| `service/ProfileService.java` — บรรทัด 10–18 | `getMyProfile()`, `updateMyProfile()`, `changeMyPassword()`, `getNavUser()` | ใช้เฉพาะหน้าโปรไฟล์ของตัวเอง |
| `service/AdminUserService.java` — บรรทัด 7–20 | `search()`, `changeRole()`, `setActive()`, `undoLast()`, `lastCommandDescription()` | ใช้เฉพาะหน้าแอดมินจัดการผู้ใช้ ผู้ใช้ทั่วไปไม่ต้องรู้จัก method พวกนี้ |
| `service/CurrentUserService.java` — บรรทัด 4–11 | มีแค่ 3 method: `getCurrentUser()`, `getCurrentUserId()`, `isAdmin()` | service อื่นที่อยากรู้แค่ "ใคร login อยู่" ขึ้นกับ interface เล็กๆ ตัวนี้ ไม่ต้องได้ method สมัครหรือแก้โปรไฟล์ติดมาด้วย |
| `service/AvatarStorageService.java` / `service/ImageStorageService.java` — บรรทัด 5–8 / 7–10 |