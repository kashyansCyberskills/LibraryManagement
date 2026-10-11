package com.anurag.lms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anurag.lms.dto.BorrowResponseDTO;
import com.anurag.lms.dto.IssueDTO;
import com.anurag.lms.dto.StatsDTO;
import com.anurag.lms.service.BorrowService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/borrow") // localhost:8080/borrow
public class BorrowController {

      private final BorrowService service;

      public BorrowController(BorrowService service) {
            this.service = service;
      }

      // staff: every borrow record, newest first
      @GetMapping("")
      public List<BorrowResponseDTO> getAll() {
            return service.getAll();
      }

      // staff: books that are past their due date and not yet returned
      @GetMapping("/overdue")
      public List<BorrowResponseDTO> getOverdue() {
            return service.getOverdue();
      }

      // staff: numbers for the dashboard
      @GetMapping("/stats")
      public StatsDTO getStats() {
            return service.getStats();
      }

      // any signed-in member: their own borrowing history
      @GetMapping("/my")
      public List<BorrowResponseDTO> getMine(Authentication auth) {
            return service.getForMember(auth.getName());
      }

      @PostMapping("/issue")
      public ResponseEntity<BorrowResponseDTO> issue(@Valid @RequestBody IssueDTO dto) {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.issueBook(dto));
      }

      // localhost:8080/borrow/return/7
      @PutMapping("/return/{id}")
      public BorrowResponseDTO returnBook(@PathVariable Long id) {
            return service.returnBook(id);
      }
}
