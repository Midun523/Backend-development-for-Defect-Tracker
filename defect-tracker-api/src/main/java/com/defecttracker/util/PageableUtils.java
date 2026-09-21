package com.defecttracker.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtils {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private PageableUtils() {
    }

    public static Pageable of(int page, int size) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        return PageRequest.of(validPage, validSize);
    }

    public static Pageable of(int page, int size, Sort sort) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        return PageRequest.of(validPage, validSize, sort != null ? sort : Sort.unsorted());
    }
}
