package com.thesis.sms_backend.auth.internal;

import com.thesis.sms_backend.core.PagedResult;
import com.thesis.sms_backend.core.UniqueConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PagedResult<User> findAll(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return PagedResult.from(userRepository.findAll(pageable));
    }

    public User getById(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public User create(CreateUserRequest request) {
        if (userRepository.existsByLogin(request.login()))
            throw new UniqueConstraintViolationException("login", request.login());

        User user = User.builder()
                .login(request.login())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(request.active())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User update(UUID uuid, UpdateUserRequest request) {
        User user = getById(uuid);

        if (request.getLogin().isPresent() && userRepository.existsByLogin(request.getLogin().getValue()))
            throw new UniqueConstraintViolationException("login", request.getLogin());

        request.getLogin().ifPresent(user::setLogin);
        request.getPassword().ifPresent((password) -> user.setPasswordHash(passwordEncoder.encode(password)));
        request.getRole().ifPresent(user::setRole);
        request.getActive().ifPresent(user::setActive);

        return userRepository.save(user);
    }

    @Transactional
    public void delete(UUID uuid) {
        User user = getById(uuid);
        userRepository.delete(user);
    }

}
