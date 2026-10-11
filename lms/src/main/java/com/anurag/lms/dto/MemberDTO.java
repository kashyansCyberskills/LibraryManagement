package com.anurag.lms.dto;

import java.time.LocalDate;

import com.anurag.lms.entity.Member;

// What the API returns for a member - the password hash is never part of it.
public record MemberDTO(Long id, String name, String email, String phone, String role, LocalDate joinedOn) {

    public static MemberDTO from(Member m) {
        return new MemberDTO(m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getRole().name(), m.getJoinedOn());
    }
}
