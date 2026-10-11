package com.anurag.lms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anurag.lms.dto.LoginDTO;
import com.anurag.lms.dto.LoginResponseDTO;
import com.anurag.lms.dto.MemberDTO;
import com.anurag.lms.dto.MemberUpdateDTO;
import com.anurag.lms.dto.RegisterDTO;
import com.anurag.lms.dto.RoleUpdateDTO;
import com.anurag.lms.service.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/members") // localhost:8080/members
public class MemberController {

      private final MemberService service;

      public MemberController(MemberService service) {
            this.service = service;
      }

      @PostMapping("/register")
      public ResponseEntity<MemberDTO> register(@Valid @RequestBody RegisterDTO dto) {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.register(dto));
      }

      @PostMapping("/login")
      public LoginResponseDTO login(@Valid @RequestBody LoginDTO dto) {
            return service.login(dto);
      }

      @GetMapping("")
      public List<MemberDTO> getAllMembers() {
            return service.getAllMembers();
      }

      // localhost:8080/members/me  -> the signed-in member's own profile
      @GetMapping("/me")
      public MemberDTO me(Authentication auth) {
            return service.getMemberByEmail(auth.getName());
      }

      // localhost:8080/members/2
      @GetMapping("/{id}")
      public MemberDTO getMemberById(@PathVariable Long id, Authentication auth) {
            MemberDTO member = service.getMemberById(id);
            assertSelfOrStaff(member, auth);
            return member;
      }

      @PutMapping("/{id}")
      public MemberDTO updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateDTO dto,
                  Authentication auth) {
            assertSelfOrStaff(service.getMemberById(id), auth);
            return service.updateMember(id, dto);
      }

      @PutMapping("/{id}/role")
      public MemberDTO updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
            return service.updateRole(id, dto.role());
      }

      @DeleteMapping("/{id}")
      public Map<String, String> deleteMember(@PathVariable Long id) {
            service.deleteMember(id);
            return Map.of("message", "Member deleted successfully");
      }

      private void assertSelfOrStaff(MemberDTO member, Authentication auth) {
            boolean staff = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                    || a.getAuthority().equals("ROLE_LIBRARIAN"));
            if (!staff && !member.email().equals(auth.getName())) {
                  throw new AccessDeniedException("You can only access your own profile");
            }
      }
}
