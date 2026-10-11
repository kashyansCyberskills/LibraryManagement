package com.anurag.lms.dto;

import com.anurag.lms.enums.Role;

import jakarta.validation.constraints.NotNull;

public record RoleUpdateDTO(@NotNull(message = "Role is required") Role role) {
}
