package com.anurag.lms.dto;

import jakarta.validation.constraints.NotNull;

public record IssueDTO(
        @NotNull(message = "Book is required") Long bookId,
        @NotNull(message = "Member is required") Long memberId) {
}
