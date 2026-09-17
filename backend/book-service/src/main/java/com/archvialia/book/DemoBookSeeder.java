package com.archvialia.book;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoBookSeeder {
    @Bean
    CommandLineRunner seedBooks(BookRepository repo, @Value("${app.seed-demo-data:false}") boolean enabled) {
        return args -> {
            if (!enabled || repo.count() > 0) return;
            repo.save(book("Clean Code", "Robert C. Martin", "Software Engineering", "Practical principles for writing readable and maintainable code.", 6));
            repo.save(book("Designing Data-Intensive Applications", "Martin Kleppmann", "Computer Science", "A guide to data systems, distributed systems and reliable architecture.", 4));
            repo.save(book("Computer Networks", "Andrew S. Tanenbaum", "Computer Science", "Foundations of computer networking, protocols and architectures.", 5));
            repo.save(book("Database System Concepts", "Abraham Silberschatz", "Databases", "Core database architecture, transactions, indexing and recovery.", 7));
            repo.save(book("The Design of Everyday Things", "Don Norman", "Design", "Human-centered design principles for products and interfaces.", 3));
            repo.save(book("Research Methodology", "Ranjit Kumar", "Research", "A practical introduction to planning and conducting academic research.", 4));
        };
    }
    private Book book(String title,String author,String category,String description,int copies){
        Book b=new Book();b.setTitle(title);b.setAuthor(author);b.setCategory(category);b.setDescription(description);b.setTotalCopies(copies);b.setAvailableCopies(copies);return b;
    }
}
