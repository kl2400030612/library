package com.archvialia.book;
import jakarta.persistence.*;
@Entity @Table(name="library_books")
public class Book {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String title;
 @Column(nullable=false) private String author;
 @Column(length=2000) private String description;
 @Column(nullable=false) private String category;
 @Column(nullable=false) private int totalCopies;
 @Column(nullable=false) private int availableCopies;
 @Column(nullable=false) private boolean active=true;
 @Column(length=1000) private String ebookUrl;
 public Long getId(){return id;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getAuthor(){return author;} public void setAuthor(String v){author=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public int getTotalCopies(){return totalCopies;} public void setTotalCopies(int v){totalCopies=v;} public int getAvailableCopies(){return availableCopies;} public void setAvailableCopies(int v){availableCopies=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public String getEbookUrl(){return ebookUrl;} public void setEbookUrl(String v){ebookUrl=v;}
}
