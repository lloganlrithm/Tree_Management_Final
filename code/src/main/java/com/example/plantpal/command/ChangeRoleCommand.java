package com.example.plantpal.command;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.repository.UserRepository;

// เปลี่ยนบทบาท USER <-> ADMIN
public class ChangeRoleCommand implements UserCommand {

    private final UserRepository userRepository;
    private final Long userId;
    private final Role newRole;
    private Role previousRole;
    private String email;

    public ChangeRoleCommand(UserRepository userRepository, Long userId, Role newRole) {
        this.userRepository = userRepository;
        this.userId = userId;
        this.newRole = newRole;
    }

    @Override
    public void execute() {
        User user = load();
        previousRole = user.getRole();     // จำค่าเดิมไว้สำหรับ undo
        email = user.getEmail();
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Override
    public void undo() {
        User user = load();
        user.setRole(previousRole);
        userRepository.save(user);
    }

    @Override
    public String description() {
        return "เปลี่ยน " + email + " เป็น" + (newRole == Role.ADMIN ? "ผู้ดูแลระบบ" : "สมาชิก");
    }

    private User load() {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("ไม่พบผู้ใช้ id " + userId));
    }
}
