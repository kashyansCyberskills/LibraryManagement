package com.anurag.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberUpdateDTO(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
        String phone,
        String password) {
}
