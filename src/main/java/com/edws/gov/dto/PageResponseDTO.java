package com.edws.gov.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int pageSize,
        long totalItems
) {

    public static <E, T> PageResponseDTO<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponseDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements()
        );
    }
}
