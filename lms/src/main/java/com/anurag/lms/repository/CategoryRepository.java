package com.anurag.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anurag.lms.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
