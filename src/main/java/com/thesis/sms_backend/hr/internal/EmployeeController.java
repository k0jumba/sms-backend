package com.thesis.sms_backend.hr.internal;

import com.thesis.sms_backend.core.ApiResponse;
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
    public ApiResponse<List<Employee>> getAll() {
        return new ApiResponse<>(true, employeeService.getAll(), null, null);
    }

    @GetMapping("/{uuid}")
    public ApiResponse<Employee> getById(@PathVariable UUID uuid) {
        return new ApiResponse<>(true, employeeService.getById(uuid), null, null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Employee> create(@RequestBody @Valid CreateEmployeeRequest request) {
        return new ApiResponse<>(true, employeeService.create(request), null, null);
    }

    @PatchMapping("/{uuid}")
    public ApiResponse<Employee> update(@PathVariable UUID uuid, @RequestBody @Valid UpdateEmployeeRequest request) {
        return new ApiResponse<>(true, employeeService.update(uuid, request), null, null);
    }

    @DeleteMapping("/{uuid}")
    public ApiResponse<Void> delete(@PathVariable UUID uuid) {
        employeeService.delete(uuid);
        return new ApiResponse<>(true, null, null, null);
    }
}