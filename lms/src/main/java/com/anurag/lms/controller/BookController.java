package com.anurag.lms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anurag.lms.dto.BookDTO;
import com.anurag.lms.entity.Book;
import com.anurag.lms.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/books") // localhost:8080/books
public class BookController {

      private final BookService service;

      public BookController(BookService service) {
            this.service = service;
      }

      // localhost:8080/books            -> everything
      // localhost:8080/books?search=sap -> title or author contains "sap"
      @GetMapping("")
      public List<Book> getAllBooks(@RequestParam(required = false) String search) {
            return service.getAllBooks(search);
      }

      // localhost:8080/books/2
      @GetMapping("/{id}")
      public Book getBookById(@PathVariable Long id) {
            return service.getBookById(id);
      }

      @PostMapping("")
      public ResponseEntity<Book> createBook(@Valid @RequestBody BookDTO dto) {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.createBook(dto));
      }

      @PutMapping("/{id}")
      public Book updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO dto) {
            return service.updateBook(id, dto);
      }

      @DeleteMapping("/{id}")
      public Map<String, String> deleteBook(@PathVariable Long id) {
            service.deleteBook(id);
            return Map.of("message", "Book deleted successfully");
      }
}
