package com.crud_project.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageState{
    public static final int DEFAULT_PAGE = 10;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;
    public static final int MIN_PAGE = 0;

    @Default
    private final int page = MIN_PAGE;
    @Default
    private final int size = DEFAULT_PAGE;

    public static boolean isValidPage(int page){
        return page >= MIN_PAGE;
    }
    public static boolean isValidSize(int size){
        return (size >= MIN_SIZE && size <= MAX_SIZE);
    }
}