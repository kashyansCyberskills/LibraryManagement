package com.anurag.lms.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.anurag.lms.dto.CategoryDTO;
import com.anurag.lms.entity.Category;
import com.anurag.lms.exception.ConflictException;
import com.anurag.lms.exception.ResourceNotFoundException;
import com.anurag.lms.repository.BookRepository;
import com.anurag.lms.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository repo;
    private final BookRepository bookRepo;

    public CategoryService(CategoryRepository repo, BookRepository bookRepo) {
        this.repo = repo;
        this.bookRepo = bookRepo;
    }

    public List<Category> getAll() {
        return repo.findAll(Sort.by("name"));
    }

    public Category create(CategoryDTO dto) {
        String name = dto.name().trim();
        if (repo.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category already exists");
        }
        Category category = new Category();
        category.setName(name);
        return repo.save(category);
    }

    public Category update(Long id, CategoryDTO dto) {
        Category category = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        String name = dto.name().trim();
        if (repo.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Category already exists");
        }
        category.setName(name);
        return repo.save(category);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (bookRepo.existsByCategoryId(id)) {
            throw new ConflictException("Books still use this category");
        }
        repo.deleteById(id);
    }
}
