package com.thesis.sms_backend.hr.internal;

import com.thesis.sms_backend.core.PagedResult;
import com.thesis.sms_backend.core.UniqueConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public PagedResult<Employee> findAll(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return PagedResult.from(employeeRepository.findAll(pageable));
    }

    public Employee getById(UUID uuid) {
        return employeeRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public Employee create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email()))
            throw new UniqueConstraintViolationException("email", request.email());
        if (employeeRepository.existsByPhone(request.phone()))
            throw new UniqueConstraintViolationException("phone", request.phone());

        Employee employee = Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .middleName(request.middleName())
                .role(request.role())
                .active(request.active())
                .email(request.email())
                .phone(request.phone())
                .build();

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(UUID uuid, UpdateEmployeeRequest request) {
        Employee employee = getById(uuid);

        if (request.getEmail().isPresent() && employeeRepository.existsByEmail(request.getEmail().getValue()))
            throw new UniqueConstraintViolationException("email", request.getEmail());
        if (request.getPhone().isPresent() && employeeRepository.existsByPhone(request.getPhone().getValue()))
            throw new UniqueConstraintViolationException("phone", request.getPhone());

        request.getFirstName().ifPresent(employee::setFirstName);
        request.getLastName().ifPresent(employee::setLastName);
        request.getMiddleName().ifPresent(employee::setMiddleName);
        request.getEmail().ifPresent(employee::setEmail);
        request.getPhone().ifPresent(employee::setPhone);
        request.getRole().ifPresent(employee::setRole);
        request.getActive().ifPresent(employee::setActive);

        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(UUID uuid) {
        Employee employee = getById(uuid);
        employeeRepository.delete(employee);
    }
}