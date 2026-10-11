package com.anurag.lms.dto;

public record LoginResponseDTO(String token, Long id, String name, String email, String role) {
}
