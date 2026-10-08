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
class SetActiveCommandTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void execute_suspendsUser_andUndo_reactivates() {
        User user = User.builder().id(3L).email("user@plantpal.com").role(Role.USER).isActive(true).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        SetActiveCommand command = new SetActiveCommand(userRepository, 3L, false);

        command.execute();
        assertThat(user.getIsActive()).isFalse();
        assertThat(command.description()).isEqualTo("ระงับบัญชี user@plantpal.com");

        command.undo();
        assertThat(user.getIsActive()).isTrue();
        verify(userRepository, times(2)).save(user);
    }
}
