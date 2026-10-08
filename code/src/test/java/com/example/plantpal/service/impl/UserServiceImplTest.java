package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;
import com.example.plantpal.repository.UserRepository;
import com.example.plantpal.validation.RegisterValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterValidator registerValidator;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findAdminIds_returnsOnlyActiveAdminIds() {
        when(userRepository.findActiveIdsByRole(Role.ADMIN)).thenReturn(List.of(1L, 5L));

        assertThat(userService.findAdminIds()).containsExactly(1L, 5L);
        verify(userRepository).findActiveIdsByRole(Role.ADMIN);
    }

    private RegisterRequest registerRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("  พรีม ");
        r.setLastName(" ทดสอบ  ");
        r.setEmail("  New.User@PlantPal.com ");
        r.setPassword("password1");
        r.setConfirmPassword("password1");
        return r;
    }

    @Test
    void register_savesUserWithHashedPassword_roleUser_andProfile() {
        when(passwordEncoder.encode("password1")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(registerRequest());

        assertThat(saved.getEmail()).isEqualTo("new.user@plantpal.com");   // ตัดช่องว่าง + ตัวเล็ก
        assertThat(saved.getPassword()).isEqualTo("HASHED");               // ไม่เก็บรหัสจริง
        assertThat(saved.getRole()).isEqualTo(Role.USER);                  // สมัครใหม่เป็น USER เสมอ
        assertThat(saved.getProfile().getFirstName()).isEqualTo("พรีม");
        assertThat(saved.getProfile().getLastName()).isEqualTo("ทดสอบ");
        assertThat(saved.getProfile().getUser()).isSameAs(saved);
    }

    @Test
    void register_whenValidationFails_doesNotSave() {
        doThrow(new RegistrationException("อีเมลนี้ถูกใช้สมัครแล้ว"))
                .when(registerValidator).validate(any(RegisterRequest.class));

        assertThatThrownBy(() -> userService.register(registerRequest()))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("อีเมลนี้ถูกใช้สมัครแล้ว");
        verify(userRepository, never()).save(any(User.class));
    }
}
