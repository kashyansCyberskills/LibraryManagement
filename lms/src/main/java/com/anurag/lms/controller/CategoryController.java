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
import org.springframework.web.bind.annotation.RestController;

import com.anurag.lms.dto.CategoryDTO;
import com.anurag.lms.entity.Category;
import com.anurag.lms.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories") // localhost:8080/categories
public class CategoryController {

      private final CategoryService service;

      public CategoryController(CategoryService service) {
            this.service = service;
      }

      @GetMapping("")
      public List<Category> getAll() {
            return service.getAll();
      }

      @PostMapping("")
      public ResponseEntity<Category> create(@Valid @RequestBody CategoryDTO dto) {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
      }

      @PutMapping("/{id}")
      public Category update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
            return service.update(id, dto);
      }

      @DeleteMapping("/{id}")
      public Map<String, String> delete(@PathVariable Long id) {
            service.delete(id);
            return Map.of("message", "Category deleted successfully");
      }
}
