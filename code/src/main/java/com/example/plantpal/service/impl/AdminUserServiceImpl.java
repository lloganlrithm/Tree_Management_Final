package com.example.plantpal.service.impl;

import com.example.plantpal.command.ChangeRoleCommand;
import com.example.plantpal.command.SetActiveCommand;
import com.example.plantpal.command.UserCommand;
import com.example.plantpal.command.UserCommandInvoker;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.entity.UserProfile;
import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.dto.response.AdminUserRow;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.repository.UserRepository;
import com.example.plantpal.service.AdminUserService;
import com.example.plantpal.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private static final int PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final UserCommandInvoker invoker;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserRow> search(String keyword, int page) {
        String q = keyword == null ? "" : keyword.trim();
        Page<User> users = userRepository.search(q,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("createdAt").descending()));

        // นับต้นไม้ของทุกคนในหน้านี้ด้วย query เดียว
        Map<Long, Long> plantCounts = new HashMap<>();
        List<Long> ids = users.map(User::getId).getContent();
        if (!ids.isEmpty()) {
            for (Object[] row : userRepository.countPlantsByUserIds(ids)) {
                plantCounts.put((Long) row[0], (Long) row[1]);
            }
        }
        Long me = currentUserService.getCurrentUserId();

        return users.map(u -> {
            UserProfile p = u.getProfile();
            String name = (p == null) ? "" : (nullToEmpty(p.getFirstName()) + " " + nullToEmpty(p.getLastName())).trim();
            return new AdminUserRow(u.getId(), u.getEmail(), name.isEmpty() ? u.getEmail() : name,
                    p == null ? null : p.getAvatarUrl(), u.getRole(), u.getIsActive(),
                    plantCounts.getOrDefault(u.getId(), 0L), u.getCreatedAt(), u.getId().equals(me));
        });
    }

    @Override
    public void changeRole(Long userId, Role role) {
        User target = loadOther(userId);
        if (target.getRole() != role) {              // ค่าเดิมอยู่แล้ว ไม่ต้องสร้างคำสั่ง
            invoker.run(new ChangeRoleCommand(userRepository, userId, role));
        }
    }

    @Override
    public void setActive(Long userId, boolean active) {
        User target = loadOther(userId);
        if (target.getIsActive() != active) {
            invoker.run(new SetActiveCommand(userRepository, userId, active));
        }
    }

    @Override
    public String undoLast() {
        UserCommand undone = invoker.undoLast();
        return undone == null ? null : undone.description();
    }

    @Override
    @Transactional(readOnly = true)
    public String lastCommandDescription() {
        UserCommand last = invoker.peekLast();
        return last == null ? null : last.description();
    }

    // admin ห้ามเปลี่ยนสิทธิ์หรือระงับตัวเอง (กันล็อกตัวเองออกจากระบบ)
    private User loadOther(Long userId) {
        if (userId.equals(currentUserService.getCurrentUserId())) {
            throw new InvalidRequestException("เปลี่ยนบทบาทหรือระงับบัญชีของตัวเองไม่ได้");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("ไม่พบผู้ใช้ที่เลือก"));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
