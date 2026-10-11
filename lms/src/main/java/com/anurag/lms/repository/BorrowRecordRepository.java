package com.anurag.lms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anurag.lms.entity.BorrowRecord;
import com.anurag.lms.enums.BorrowStatus;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findAllByOrderByIssueDateDescIdDesc();

    List<BorrowRecord> findByMemberIdOrderByIssueDateDescIdDesc(Long memberId);

    List<BorrowRecord> findByStatusAndDueDateBeforeOrderByDueDateAsc(BorrowStatus status, LocalDate date);

    long countByStatus(BorrowStatus status);

    long countByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);

    long countByMemberIdAndStatus(Long memberId, BorrowStatus status);

    boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, BorrowStatus status);

    boolean existsByBookIdAndStatus(Long bookId, BorrowStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, BorrowStatus status);
}
