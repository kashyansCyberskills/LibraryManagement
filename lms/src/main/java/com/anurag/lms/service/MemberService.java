package com.anurag.lms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.anurag.lms.dto.LoginDTO;
import com.anurag.lms.dto.LoginResponseDTO;
import com.anurag.lms.dto.MemberDTO;
import com.anurag.lms.dto.MemberUpdateDTO;
import com.anurag.lms.dto.RegisterDTO;
import com.anurag.lms.entity.Member;
import com.anurag.lms.enums.BorrowStatus;
import com.anurag.lms.enums.Role;
import com.anurag.lms.exception.BadRequestException;
import com.anurag.lms.exception.ConflictException;
import com.anurag.lms.exception.ResourceNotFoundException;
import com.anurag.lms.repository.BorrowRecordRepository;
import com.anurag.lms.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository repo;
    private final BorrowRecordRepository borrowRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository repo, BorrowRecordRepository borrowRepo,
                         JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.borrowRepo = borrowRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private Member find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    // Public sign-up always creates a MEMBER. Staff roles are granted by an admin (see updateRole).
    public MemberDTO register(RegisterDTO dto) {
        String email = normalize(dto.email());
        if (repo.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        Member member = new Member();
        member.setName(dto.name().trim());
        member.setEmail(email);
        member.setPhone(dto.phone() == null ? null : dto.phone().trim());
        member.setPassword(passwordEncoder.encode(dto.password()));
        member.setRole(Role.MEMBER);
        member.setJoinedOn(LocalDate.now());
        return MemberDTO.from(repo.save(member));
    }

    public LoginResponseDTO login(LoginDTO dto) {
        Member member = repo.findByEmail(normalize(dto.email()))
                .orElseThrow(() -> new BadCredentialsException("Email or password is wrong"));
        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new BadCredentialsException("Email or password is wrong");
        }
        String token = jwtService.generateToken(member.getEmail());
        return new LoginResponseDTO(token, member.getId(), member.getName(), member.getEmail(),
                member.getRole().name());
    }

    public List<MemberDTO> getAllMembers() {
        return repo.findAll(Sort.by("name")).stream().map(MemberDTO::from).toList();
    }

    public MemberDTO getMemberById(Long id) {
        return MemberDTO.from(find(id));
    }

    public MemberDTO getMemberByEmail(String email) {
        return MemberDTO.from(repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found")));
    }

    public MemberDTO updateMember(Long id, MemberUpdateDTO dto) {
        Member member = find(id);
        String email = normalize(dto.email());
        if (repo.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("Another account already uses this email");
        }
        member.setName(dto.name().trim());
        member.setEmail(email);
        member.setPhone(dto.phone() == null ? null : dto.phone().trim());
        if (dto.password() != null && !dto.password().isBlank()) {
            if (dto.password().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters");
            }
            member.setPassword(passwordEncoder.encode(dto.password()));
        }
        return MemberDTO.from(repo.save(member));
    }

    public MemberDTO updateRole(Long id, Role newRole) {
        Member member = find(id);
        if (member.getRole() == Role.ADMIN && newRole != Role.ADMIN
                && repo.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("At least one admin must remain");
        }
        member.setRole(newRole);
        return MemberDTO.from(repo.save(member));
    }

    public void deleteMember(Long id) {
        Member member = find(id);
        if (member.getRole() == Role.ADMIN && repo.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("At least one admin must remain");
        }
        if (borrowRepo.existsByMemberIdAndStatus(id, BorrowStatus.ISSUED)) {
            throw new ConflictException("This member still has books issued");
        }
        repo.delete(member);
    }
}
