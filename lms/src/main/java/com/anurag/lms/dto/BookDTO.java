package com.anurag.lms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookDTO(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Author is required") String author,
        @NotBlank(message = "ISBN is required") String isbn,
        @Min(value = 1000, message = "Enter a valid year") @Max(value = 2100, message = "Enter a valid year") Integer publishedYear,
        @NotNull(message = "Total copies is required") @Min(value = 1, message = "At least 1 copy is required") Integer totalCopies,
        Long categoryId) {
}
