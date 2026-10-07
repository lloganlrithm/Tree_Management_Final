package com.example.plantpal.command;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.repository.UserRepository;

// ระงับ / เปิดใช้บัญชี (users.is_active)
public class SetActiveCommand implements UserCommand {

    private final UserRepository userRepository;
    private final Long userId;
    private final boolean active;
    private boolean previousActive;
    private String email;

    public SetActiveCommand(UserRepository userRepository, Long userId, boolean active) {
        this.userRepository = userRepository;
        this.userId = userId;
        this.active = active;
    }

    @Override
    public void execute() {
        User user = load();
        previousActive = user.getIsActive();
        email = user.getEmail();
        user.setIsActive(active);
        userRepository.save(user);
    }

    @Override
    public void undo() {
        User user = load();
        user.setIsActive(previousActive);
        userRepository.save(user);
    }

    @Override
    public String description() {
        return (active ? "เปิดใช้บัญชี " : "ระงับบัญชี ") + email;
    }

    private User load() {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("ไม่พบผู้ใช้ id " + userId));
    }
}
