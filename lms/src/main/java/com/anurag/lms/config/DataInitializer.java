package com.anurag.lms.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.anurag.lms.entity.Book;
import com.anurag.lms.entity.Category;
import com.anurag.lms.entity.Member;
import com.anurag.lms.enums.Role;
import com.anurag.lms.repository.BookRepository;
import com.anurag.lms.repository.CategoryRepository;
import com.anurag.lms.repository.MemberRepository;

@Configuration
public class DataInitializer {

    // Public sign-up only creates MEMBER accounts, so the first admin has to come from here.
    @Bean
    CommandLineRunner seedAdmin(MemberRepository members, PasswordEncoder encoder,
                                @Value("${app.admin.name}") String name,
                                @Value("${app.admin.email}") String email,
                                @Value("${app.admin.password}") String password) {
        return args -> {
            if (members.countByRole(Role.ADMIN) == 0) {
                Member admin = new Member();
                admin.setName(name);
                admin.setEmail(email.trim().toLowerCase());
                admin.setPassword(encoder.encode(password));
                admin.setRole(Role.ADMIN);
                admin.setJoinedOn(LocalDate.now());
                members.save(admin);
            }
        };
    }

    // A few sample books so the catalogue is not empty on first run. Turn off with app.seed-sample-data=false.
    @Bean
    CommandLineRunner seedSampleBooks(BookRepository books, CategoryRepository categories,
                                      @Value("${app.seed-sample-data:false}") boolean enabled) {
        return args -> {
            if (!enabled || books.count() > 0) {
                return;
            }
            Category technology = category(categories, "Technology");
            Category science = category(categories, "Science");
            Category history = category(categories, "History");
            Category fiction = category(categories, "Fiction");
            Category biography = category(categories, "Biography");

            books.save(book("Clean Code", "Robert C. Martin", "9780132350884", 2008, 3, technology));
            books.save(book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "9780135957059", 2019, 2, technology));
            books.save(book("A Brief History of Time", "Stephen Hawking", "9780553380163", 1988, 2, science));
            books.save(book("Sapiens", "Yuval Noah Harari", "9780062316097", 2015, 4, history));
            books.save(book("The Alchemist", "Paulo Coelho", "9780062315007", 1988, 3, fiction));
            books.save(book("Wings of Fire", "A. P. J. Abdul Kalam", "9788173711466", 1999, 2, biography));
        };
    }

    private Category category(CategoryRepository repo, String name) {
        Category c = new Category();
        c.setName(name);
        return repo.save(c);
    }

    private Book book(String title, String author, String isbn, int year, int copies, Category category) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setPublishedYear(year);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        b.setCategory(category);
        return b;
    }
}
