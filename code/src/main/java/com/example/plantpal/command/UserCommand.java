package com.example.plantpal.command;

// Command pattern: ห่อ "คำสั่งที่ admin ทำกับผู้ใช้" เป็น object เดียว
// เก็บค่าเดิมไว้ในตัว เลยย้อนกลับ (undo) ได้
public interface UserCommand {

    void execute();

    void undo();

    // ข้อความอธิบายคำสั่ง ใช้แสดงในแถบ "ย้อนกลับ"
    String description();
}
