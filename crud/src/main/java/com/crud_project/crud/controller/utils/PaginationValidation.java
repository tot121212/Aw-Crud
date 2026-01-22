package com.crud_project.crud.controller.utils;

import com.crud_project.crud.entity.PageState;

public class PaginationValidation {
    public static boolean isValidPageNumber(Integer pageNumber){
        return pageNumber != null && pageNumber >= 0;
    }
    public static boolean isValidPageSize(Integer pageSize){
        return pageSize != null && pageSize >= 0 && pageSize <= PageState.MAX_SIZE;
    }
}
