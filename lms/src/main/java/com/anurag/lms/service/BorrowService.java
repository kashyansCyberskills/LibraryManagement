package com.anurag.lms.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anurag.lms.dto.BorrowResponseDTO;
import com.anurag.lms.dto.IssueDTO;
import com.anurag.lms.dto.StatsDTO;
import com.anurag.lms.entity.Book;
import com.anurag.lms.entity.BorrowRecord;
import com.anurag.lms.entity.Member;
import com.anurag.lms.enums.BorrowStatus;
import com.anurag.lms.exception.ConflictException;
import com.anurag.lms.exception.ResourceNotFoundException;
import com.anurag.lms.repository.BookRepository;
import com.anurag.lms.repository.BorrowRecordRepository;
import com.anurag.lms.repository.MemberRepository;

@Service
public class BorrowService {

    private final BorrowRecordRepository repo;
    private final BookRepository bookRepo;
    private final MemberRepository memberRepo;

    private final int loanDays;
    private final BigDecimal finePerDay;
    private final int maxActiveLoans;

    public BorrowService(BorrowRecordRepository repo, BookRepository bookRepo, MemberRepository memberRepo,
                         @Value("${library.loan-days}") int loanDays,
                         @Value("${library.fine-per-day}") BigDecimal finePerDay,
                         @Value("${library.max-active-loans}") int maxActiveLoans) {
        this.repo = repo;
        this.bookRepo = bookRepo;
        this.memberRepo = memberRepo;
        this.loanDays = loanDays;
        this.finePerDay = finePerDay;
        this.maxActiveLoans = maxActiveLoans;
    }

    private BigDecimal calculateFine(LocalDate dueDate, LocalDate until) {
        long lateDays = ChronoUnit.DAYS.between(dueDate, until);
        return lateDays > 0 ? finePerDay.multiply(BigDecimal.valueOf(lateDays)) : BigDecimal.ZERO;
    }

    private BorrowResponseDTO toDto(BorrowRecord r) {
        boolean issued = r.getStatus() == BorrowStatus.ISSUED;
        boolean overdue = issued && LocalDate.now().isAfter(r.getDueDate());
        // For a book that is still out, show the fine accrued so far.
        BigDecimal fine = issued ? calculateFine(r.getDueDate(), LocalDate.now()) : r.getFine();
        return new BorrowResponseDTO(r.getId(), r.getBook().getId(), r.getBook().getTitle(),
                r.getMember().getId(), r.getMember().getName(), r.getIssueDate(), r.getDueDate(),
                r.getReturnDate(), fine, r.getStatus().name(), overdue);
    }

    @Transactional
    public BorrowResponseDTO issueBook(IssueDTO dto) {
        Book book = bookRepo.findById(dto.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        Member member = memberRepo.findById(dto.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new ConflictException("No copies of this book are available right now");
        }
        if (repo.existsByBookIdAndMemberIdAndStatus(book.getId(), member.getId(), BorrowStatus.ISSUED)) {
            throw new ConflictException("This member already has a copy of this book");
        }
        if (repo.countByMemberIdAndStatus(member.getId(), BorrowStatus.ISSUED) >= maxActiveLoans) {
            throw new ConflictException("This member has reached the limit of " + maxActiveLoans + " books");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepo.save(book);

        BorrowRecord record = new BorrowRecord();
        record.setBook(book);
        record.setMember(member);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(loanDays));
        record.setFine(BigDecimal.ZERO);
        record.setStatus(BorrowStatus.ISSUED);
        return toDto(repo.save(record));
    }

    @Transactional
    public BorrowResponseDTO returnBook(Long recordId) {
        BorrowRecord record = repo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new ConflictException("This book has already been returned");
        }

        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        record.setFine(calculateFine(record.getDueDate(), today));
        record.setStatus(BorrowStatus.RETURNED);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepo.save(book);

        return toDto(repo.save(record));
    }

    public List<BorrowResponseDTO> getAll() {
        return repo.findAllByOrderByIssueDateDescIdDesc().stream().map(this::toDto).toList();
    }

    public List<BorrowResponseDTO> getOverdue() {
        return repo.findByStatusAndDueDateBeforeOrderByDueDateAsc(BorrowStatus.ISSUED, LocalDate.now())
                .stream().map(this::toDto).toList();
    }

    public List<BorrowResponseDTO> getForMember(String email) {
        Member member = memberRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        return repo.findByMemberIdOrderByIssueDateDescIdDesc(member.getId()).stream().map(this::toDto).toList();
    }

    public StatsDTO getStats() {
        LocalDate today = LocalDate.now();
        return new StatsDTO(bookRepo.count(), memberRepo.count(),
                repo.countByStatus(BorrowStatus.ISSUED),
                repo.countByStatusAndDueDateBefore(BorrowStatus.ISSUED, today));
    }
}
