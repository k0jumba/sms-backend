package com.thesis.sms_backend.auth;

import com.thesis.sms_backend.auth.internal.*;
import com.thesis.sms_backend.core.Patch;
import com.thesis.sms_backend.core.UniqueConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = User.builder()
                .login("jane_doe")
                .passwordHash("hashed_password")
                .role(User.Role.ADMIN)
                .active(true)
                .build();
    }

    @Nested
    class GetById {

        @Test
        void returnsUserWhenFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            User result = userService.getById(userId);

            assertThat(result).isSameAs(existingUser);
        }

        @Test
        void throwsEntityNotFoundExceptionWhenNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(userId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Create {

        private CreateUserRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateUserRequest(
                    "jane_doe",
                    "secret123",
                    User.Role.ADMIN,
                    true
            );
        }

        @Test
        void throwsUniqueConstraintViolationExceptionWhenLoginIsTaken() {
            when(userRepository.existsByLogin(request.login())).thenReturn(true);

            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(UniqueConstraintViolationException.class)
                    .satisfies(ex -> assertThat(
                            ((UniqueConstraintViolationException) ex).getField()
                    ).isEqualTo("login"));

            verify(userRepository, never()).save(any());
        }

        @Test
        void savesUserWithCorrectFieldValuesAndReturnsIt() {
            when(userRepository.existsByLogin(request.login())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encoded_secret123");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.create(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User saved = captor.getValue();
            assertThat(saved.getLogin()).isEqualTo(request.login());
            assertThat(saved.getPasswordHash()).isEqualTo("encoded_secret123");
            assertThat(saved.getRole()).isEqualTo(request.role());
            assertThat(saved.isActive()).isEqualTo(request.active());

            assertThat(result).isSameAs(saved);
        }
    }

    @Nested
    class Update {

        private UpdateUserRequest fullUpdateRequest;
        private UpdateUserRequest emptyUpdateRequest;

        @BeforeEach
        void setUp() {
            fullUpdateRequest = UpdateUserRequest.builder()
                    .login(Patch.of("john_smith"))
                    .password(Patch.of("newpassword"))
                    .role(Patch.of(User.Role.MANAGER))
                    .active(Patch.of(false))
                    .build();

            emptyUpdateRequest = UpdateUserRequest.builder()
                    .login(Patch.absent())
                    .password(Patch.absent())
                    .role(Patch.absent())
                    .active(Patch.absent())
                    .build();
        }

        @Test
        void throwsEntityNotFoundExceptionWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(userId, fullUpdateRequest))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        void throwsUniqueConstraintViolationExceptionWhenNewLoginIsTaken() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByLogin(fullUpdateRequest.getLogin().getValue()))
                    .thenReturn(true);

            assertThatThrownBy(() -> userService.update(userId, fullUpdateRequest))
                    .isInstanceOf(UniqueConstraintViolationException.class)
                    .satisfies(ex -> assertThat(
                            ((UniqueConstraintViolationException) ex).getField()
                    ).isEqualTo("login"));

            verify(userRepository, never()).save(any());
        }

        @Test
        void appliesOnlyPresentFieldsAndSaves() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByLogin(fullUpdateRequest.getLogin().getValue()))
                    .thenReturn(false);
            when(passwordEncoder.encode(fullUpdateRequest.getPassword().getValue()))
                    .thenReturn("encoded_newpassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.update(userId, fullUpdateRequest);

            assertThat(existingUser.getLogin()).isEqualTo("john_smith");
            assertThat(existingUser.getPasswordHash()).isEqualTo("encoded_newpassword");
            assertThat(existingUser.getRole()).isEqualTo(User.Role.MANAGER);
            assertThat(existingUser.isActive()).isFalse();

            verify(userRepository).save(existingUser);
            assertThat(result).isSameAs(existingUser);
        }

        @Test
        void skipsUniquenessCheckAndLeavesFieldsUnchangedWhenAllFieldsAbsent() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.update(userId, emptyUpdateRequest);

            verify(userRepository, never()).existsByLogin(any());

            assertThat(existingUser.getLogin()).isEqualTo("jane_doe");
            assertThat(existingUser.getPasswordHash()).isEqualTo("hashed_password");

            verify(userRepository).save(existingUser);
        }
    }

    @Nested
    class Delete {

        @Test
        void throwsEntityNotFoundExceptionWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.delete(userId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }

        @Test
        void callsRepositoryDeleteWithTheCorrectUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            userService.delete(userId);

            verify(userRepository).delete(existingUser);
        }
    }
}