package com.thesis.sms_backend.hr.internal;

import com.thesis.sms_backend.core.ApiMeta;
import com.thesis.sms_backend.core.ApiResponse;
import com.thesis.sms_backend.core.PagedResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/hr/employees")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Employee>>> getAll(
            @RequestParam(defaultValue = "0") @Min(0)  int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        PagedResult<Employee> result = employeeService.findAll(page, pageSize);

        ApiMeta meta = new ApiMeta()
                .add("page",          result.page())
                .add("pageSize",      result.pageSize())
                .add("totalElements", result.totalElements())
                .add("totalPages",    result.totalPages());

        return ResponseEntity.ok(new ApiResponse<>(true, result.content(), null, meta));
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