package com.resume.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class Page<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;

    public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }

    public static <T> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        return new Page<>(content, pageNumber, pageSize, totalElements);
    }
}
