package com.wahid.everyDollar.requests.catgories;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    @NotNull(message = "Category name can not be null")
    @NotEmpty(message = "Category name can not be empty")
    private String name;

    @NotNull(message = "Category id can not be null")
    @PositiveOrZero(message = "Category id can not be less than one")
    private Long id;
}
