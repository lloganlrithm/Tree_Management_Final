package com.example.plantpal.validation;

import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;
import com.example.plantpal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Chain of Responsibility ตอนสมัคร: อีเมลถูกรูปแบบ -> อีเมลไม่ซ้ำ -> รหัสยาวพอ -> รหัสตรงกัน
@ExtendWith(MockitoExtension.class)
class RegisterValidationChainTest {

    @Mock
    private UserRepository userRepository;

    private RegisterValidator chain;

    @BeforeEach
    void setUp() {
        chain = new RegisterValidationChain().registerValidator(userRepository);
    }

    private RegisterRequest request(String email, String password, String confirm) {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("พรีม");
        r.setLastName("ทดสอบ");
        r.setEmail(email);
        r.setPassword(password);
        r.setConfirmPassword(confirm);
        return r;
    }

    @Test
    void validRequest_passesWholeChain() {
        when(userRepository.existsByEmail("new@plantpal.com")).thenReturn(false);

        assertThatCode(() -> chain.validate(request("new@plantpal.com", "password1", "password1")))
                .doesNotThrowAnyException();
    }

    @Test
    void invalidEmail_stopsAtFirstStep_andNeverQueriesDatabase() {
        assertThatThrownBy(() -> chain.validate(request("not-an-email", "password1", "password1")))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("รูปแบบอีเมลไม่ถูกต้อง");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void emailAlreadyUsed_isRejected() {
        when(userRepository.existsByEmail("used@plantpal.com")).thenReturn(true);

        assertThatThrownBy(() -> chain.validate(request("used@plantpal.com", "password1", "password1")))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("อีเมลนี้ถูกใช้สมัครแล้ว");
    }

    @Test
    void passwordShorterThan8_isRejected() {
        when(userRepository.existsByEmail("new@plantpal.com")).thenReturn(false);

        assertThatThrownBy(() -> chain.validate(request("new@plantpal.com", "short", "short")))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร");
    }

    @Test
    void confirmPasswordMismatch_isRejected() {
        when(userRepository.existsByEmail("new@plantpal.com")).thenReturn(false);

        assertThatThrownBy(() -> chain.validate(request("new@plantpal.com", "password1", "password2")))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("รหัสผ่านไม่ตรงกัน");
    }
}
