package com.rentivo.rental_saas.common.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private int pageNumber;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}