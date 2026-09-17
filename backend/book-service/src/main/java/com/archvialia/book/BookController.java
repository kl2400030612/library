package com.archvialia.book;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
 private final BookRepository repo;
 public BookController(BookRepository repo){this.repo=repo;}

 @GetMapping public List<BookResponse> all(){return repo.findAll().stream().filter(Book::isActive).map(BookResponse::from).toList();}
 @GetMapping("/search") public List<BookResponse> search(@RequestParam(defaultValue="") String q){return repo.search(q.trim()).stream().map(BookResponse::from).toList();}
 @GetMapping("/{id}") public BookResponse get(@PathVariable Long id){return repo.findById(id).filter(Book::isActive).map(BookResponse::from).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Book not found"));}
 @GetMapping("/{id}/availability") public Availability availability(@PathVariable Long id){Book b=find(id);return new Availability(b.getId(),b.getAvailableCopies(),b.getTotalCopies(),b.getAvailableCopies()>0);}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public BookResponse create(@Valid @RequestBody BookRequest r){if(r.availableCopies()>r.totalCopies()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Available copies cannot exceed total copies"); Book b=new Book(); apply(b,r); return BookResponse.from(repo.save(b));}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public BookResponse update(@PathVariable Long id,@Valid @RequestBody BookRequest r){Book b=find(id); if(r.availableCopies()>r.totalCopies()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Available copies cannot exceed total copies"); apply(b,r); return BookResponse.from(repo.save(b));}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable Long id){Book b=find(id);b.setActive(false);repo.save(b);}

 @PostMapping("/{id}/reserve") @PreAuthorize("isAuthenticated()") public Availability reserve(@PathVariable Long id){Book b=find(id); if(b.getAvailableCopies()<=0) throw new ResponseStatusException(HttpStatus.CONFLICT,"No available copies"); b.setAvailableCopies(b.getAvailableCopies()-1); repo.save(b); return new Availability(b.getId(),b.getAvailableCopies(),b.getTotalCopies(),b.getAvailableCopies()>0);}
 @PostMapping("/{id}/release") @PreAuthorize("isAuthenticated()") public Availability release(@PathVariable Long id){Book b=find(id); if(b.getAvailableCopies()>=b.getTotalCopies()) throw new ResponseStatusException(HttpStatus.CONFLICT,"All copies are already available"); b.setAvailableCopies(b.getAvailableCopies()+1); repo.save(b); return new Availability(b.getId(),b.getAvailableCopies(),b.getTotalCopies(),true);}
 @GetMapping("/health") public String health(){return "book-service:UP";}

 private Book find(Long id){return repo.findById(id).filter(Book::isActive).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Book not found"));}
 private void apply(Book b,BookRequest r){b.setTitle(r.title().trim());b.setAuthor(r.author().trim());b.setDescription(r.description());b.setCategory(r.category().trim());b.setTotalCopies(r.totalCopies());b.setAvailableCopies(r.availableCopies());b.setEbookUrl(r.ebookUrl());b.setActive(true);}
 public record BookRequest(@NotBlank String title,@NotBlank String author,String description,@NotBlank String category,@Min(1) int totalCopies,@Min(0) int availableCopies,String ebookUrl){}
 public record BookResponse(Long id,String title,String author,String description,String category,int totalCopies,int availableCopies,boolean active,String ebookUrl){static BookResponse from(Book b){return new BookResponse(b.getId(),b.getTitle(),b.getAuthor(),b.getDescription(),b.getCategory(),b.getTotalCopies(),b.getAvailableCopies(),b.isActive(),b.getEbookUrl());}}
 public record Availability(Long bookId,int availableCopies,int totalCopies,boolean available){}
}
