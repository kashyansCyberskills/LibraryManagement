package com.anurag.lms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BorrowResponseDTO(
        Long id,
        Long bookId,
        String bookTitle,
        Long memberId,
        String memberName,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate returnDate,
        BigDecimal fine,
        String status,
        boolean overdue) {
}
