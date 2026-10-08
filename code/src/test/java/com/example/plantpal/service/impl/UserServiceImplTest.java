package com.example.plantpal.service.impl;

import com.example.plantpal.domain.enums.Role;
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
}