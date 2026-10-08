package com.example.plantpal.command;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeRoleCommandTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void execute_changesRole_andUndo_restoresPreviousRole() {
        User user = User.builder().id(7L).email("user@plantpal.com").role(Role.USER).isActive(true).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        ChangeRoleCommand command = new ChangeRoleCommand(userRepository, 7L, Role.ADMIN);

        command.execute();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(command.description()).contains("user@plantpal.com").contains("ผู้ดูแลระบบ");

        command.undo();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        verify(userRepository, times(2)).save(user);
    }
}
