package com.example.plantpal.command;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayDeque;
import java.util.Deque;

// Invoker: สั่งรันคำสั่งและจำประวัติไว้ ย้อนกลับได้ทีละคำสั่ง (ล่าสุดก่อน)
// @SessionScope = admin แต่ละคนมีประวัติของตัวเอง ไม่ปนกัน
@Component
@SessionScope
public class UserCommandInvoker {

    private static final int MAX_HISTORY = 10;

    private final Deque<UserCommand> history = new ArrayDeque<>();

    public void run(UserCommand command) {
        command.execute();
        history.push(command);
        if (history.size() > MAX_HISTORY) {
            history.removeLast();      // เก็บแค่ 10 คำสั่งล่าสุด
        }
    }

    // คืนคำสั่งที่ถูกย้อนกลับ หรือ null ถ้าไม่มีอะไรให้ย้อน
    public UserCommand undoLast() {
        UserCommand last = history.poll();
        if (last != null) {
            last.undo();
        }
        return last;
    }

    public UserCommand peekLast() {
        return history.peek();
    }
}
