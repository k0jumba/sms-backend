package com.thesis.sms_backend.hr.internal;

import com.thesis.sms_backend.core.UniqueConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> getAll() {
        return employeeRepository.findAll();
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

        if (request.firstName() != null)  employee.setFirstName(request.firstName());
        if (request.lastName() != null)   employee.setLastName(request.lastName());
        if (request.middleName() != null) employee.setMiddleName(request.middleName());
        if (request.role() != null)       employee.setRole(request.role());
        if (request.active() != null)     employee.setActive(request.active());
        if (request.email() != null)      employee.setEmail(request.email());
        if (request.phone() != null)      employee.setPhone(request.phone());

        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(UUID uuid) {
        Employee employee = getById(uuid);
        employeeRepository.delete(employee);
    }
}