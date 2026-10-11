package com.anurag.lms.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anurag.lms.dto.BookDTO;
import com.anurag.lms.entity.Book;
import com.anurag.lms.entity.Category;
import com.anurag.lms.enums.BorrowStatus;
import com.anurag.lms.exception.BadRequestException;
import com.anurag.lms.exception.ConflictException;
import com.anurag.lms.exception.ResourceNotFoundException;
import com.anurag.lms.repository.BookRepository;
import com.anurag.lms.repository.BorrowRecordRepository;
import com.anurag.lms.repository.CategoryRepository;

@Service
public class BookService {

    private final BookRepository repo;
    private final CategoryRepository categoryRepo;
    private final BorrowRecordRepository borrowRepo;

    public BookService(BookRepository repo, CategoryRepository categoryRepo, BorrowRecordRepository borrowRepo) {
        this.repo = repo;
        this.categoryRepo = categoryRepo;
        this.borrowRepo = borrowRepo;
    }

    public List<Book> getAllBooks(String search) {
        if (search == null || search.isBlank()) {
            return repo.findAll(Sort.by("title"));
        }
        String term = search.trim();
        return repo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(term, term);
    }

    public Book getBookById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepo.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("Category does not exist"));
    }

    public Book createBook(BookDTO dto) {
        String isbn = dto.isbn().trim();
        if (repo.existsByIsbn(isbn)) {
            throw new ConflictException("A book with this ISBN already exists");
        }
        Book book = new Book();
        book.setTitle(dto.title().trim());
        book.setAuthor(dto.author().trim());
        book.setIsbn(isbn);
        book.setPublishedYear(dto.publishedYear());
        book.setTotalCopies(dto.totalCopies());
        book.setAvailableCopies(dto.totalCopies());
        book.setCategory(resolveCategory(dto.categoryId()));
        return repo.save(book);
    }

    @Transactional
    public Book updateBook(Long id, BookDTO dto) {
        Book book = getBookById(id);
        String isbn = dto.isbn().trim();
        if (repo.existsByIsbnAndIdNot(isbn, id)) {
            throw new ConflictException("A book with this ISBN already exists");
        }
        int issued = book.getTotalCopies() - book.getAvailableCopies();
        if (dto.totalCopies() < issued) {
            throw new BadRequestException(
                    "Total copies cannot be lower than the " + issued + " copies currently issued");
        }
        book.setTitle(dto.title().trim());
        book.setAuthor(dto.author().trim());
        book.setIsbn(isbn);
        book.setPublishedYear(dto.publishedYear());
        book.setTotalCopies(dto.totalCopies());
        book.setAvailableCopies(dto.totalCopies() - issued);
        book.setCategory(resolveCategory(dto.categoryId()));
        return repo.save(book);
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        if (borrowRepo.existsByBookIdAndStatus(id, BorrowStatus.ISSUED)) {
            throw new ConflictException("Copies of this book are still issued");
        }
        repo.delete(book);
    }
}
