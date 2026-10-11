package com.anurag.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anurag.lms.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    boolean existsByCategoryId(Long categoryId);
}
