package com.thesis.sms_backend.auth.internal;

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
@RequestMapping("api/auth/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAll(
            @RequestParam(defaultValue = "0") @Min(0)  int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        PagedResult<User> result = userService.findAll(page, pageSize);

        ApiMeta meta = new ApiMeta()
                .add("page",          result.page())
                .add("pageSize",      result.pageSize())
                .add("totalElements", result.totalElements())
                .add("totalPages",    result.totalPages());

        return ResponseEntity.ok(new ApiResponse<>(true, result.content(), null, meta));
    }

    @GetMapping("/{uuid}")
    public ApiResponse<User> getById(@PathVariable UUID uuid) {
        return new ApiResponse<>(true, userService.getById(uuid), null, null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> create(@RequestBody @Valid CreateUserRequest request) {
        return new ApiResponse<>(true, userService.create(request), null, null);
    }

    @PatchMapping("/{uuid}")
    public ApiResponse<User> update(@PathVariable UUID uuid, @RequestBody @Valid UpdateUserRequest request) {
        return new ApiResponse<>(true, userService.update(uuid, request), null, null);
    }

    @DeleteMapping("/{uuid}")
    public ApiResponse<Void> delete(@PathVariable UUID uuid) {
        userService.delete(uuid);
        return new ApiResponse<>(true, null, null, null);
    }
}