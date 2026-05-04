package com.thesis.sms_backend.hr.internal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<Employee> getAll() {
        return employeeService.getAll();
    }

    @GetMapping("/{uuid}")
    public Employee getById(@PathVariable UUID uuid) {
        return employeeService.getById(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee create(@RequestBody @Valid CreateEmployeeRequest request) {
        return employeeService.create(request);
    }

    @PatchMapping("/{uuid}")
    public Employee update(@PathVariable UUID uuid, @RequestBody @Valid UpdateEmployeeRequest request) {
        return employeeService.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID uuid) {
        employeeService.delete(uuid);
    }
}