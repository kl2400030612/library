package com.archvialia.borrow;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="library_borrow_records")
public class BorrowRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long userId;
 @Column(nullable=false) private Long bookId;
 @Column(nullable=false) private LocalDate borrowDate;
 @Column(nullable=false) private LocalDate dueDate;
 private LocalDate returnDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private BorrowStatus status=BorrowStatus.ACTIVE;
 public Long getId(){return id;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public Long getBookId(){return bookId;} public void setBookId(Long v){bookId=v;} public LocalDate getBorrowDate(){return borrowDate;} public void setBorrowDate(LocalDate v){borrowDate=v;} public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;} public LocalDate getReturnDate(){return returnDate;} public void setReturnDate(LocalDate v){returnDate=v;} public BorrowStatus getStatus(){return status;} public void setStatus(BorrowStatus v){status=v;}
}
