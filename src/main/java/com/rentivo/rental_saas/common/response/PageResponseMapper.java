package com.rentivo.rental_saas.common.response;

import org.springframework.data.domain.Page;

public class PageResponseMapper {

    private PageResponseMapper() {
    }

    public static <T> PageResponseDto<T> toPageResponse(Page<T> page) {
        return PageResponseDto.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}