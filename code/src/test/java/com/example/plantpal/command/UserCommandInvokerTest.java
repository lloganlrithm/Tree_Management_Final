package com.example.plantpal.command;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Invoker: รันคำสั่ง จำประวัติ และย้อนกลับแบบล่าสุดก่อน (LIFO) เก็บไม่เกิน 10 คำสั่ง
class UserCommandInvokerTest {

    // คำสั่งปลอมไว้นับว่าถูก execute / undo ตามลำดับไหน
    private static class FakeCommand implements UserCommand {
        private final String name;
        private final List<String> log;

        FakeCommand(String name, List<String> log) {
            this.name = name;
            this.log = log;
        }

        @Override public void execute() { log.add("do " + name); }
        @Override public void undo() { log.add("undo " + name); }
        @Override public String description() { return name; }
    }

    @Test
    void undoLast_undoesMostRecentCommandFirst() {
        List<String> log = new ArrayList<>();
        UserCommandInvoker invoker = new UserCommandInvoker();
        invoker.run(new FakeCommand("A", log));
        invoker.run(new FakeCommand("B", log));

        assertThat(invoker.peekLast().description()).isEqualTo("B");
        assertThat(invoker.undoLast().description()).isEqualTo("B");
        assertThat(invoker.undoLast().description()).isEqualTo("A");
        assertThat(log).containsExactly("do A", "do B", "undo B", "undo A");
    }

    @Test
    void undoLast_withEmptyHistory_returnsNull() {
        UserCommandInvoker invoker = new UserCommandInvoker();

        assertThat(invoker.undoLast()).isNull();
        assertThat(invoker.peekLast()).isNull();
    }

    @Test
    void keepsOnlyLatest10Commands() {
        List<String> log = new ArrayList<>();
        UserCommandInvoker invoker = new UserCommandInvoker();
        for (int i = 1; i <= 11; i++) {
            invoker.run(new FakeCommand("C" + i, log));
        }

        int undone = 0;
        while (invoker.undoLast() != null) {
            undone++;
        }
        assertThat(undone).isEqualTo(10);
        assertThat(log).doesNotContain("undo C1");   // คำสั่งแรกสุดหลุดจากประวัติไปแล้ว
    }
}
