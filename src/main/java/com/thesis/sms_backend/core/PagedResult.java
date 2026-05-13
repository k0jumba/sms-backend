package com.thesis.sms_backend.core;

import java.util.List;

public record PagedResult<T>(
        List<T> content,
        int page,
        int pageSize,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResult<T> from(org.springframework.data.domain.Page<T> page) {
        return new PagedResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
