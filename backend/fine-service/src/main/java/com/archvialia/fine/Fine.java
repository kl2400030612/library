package com.archvialia.fine;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="library_fines", uniqueConstraints=@UniqueConstraint(name="uk_fine_borrow",columnNames="borrowId"))
public class Fine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long borrowId;
 @Column(nullable=false) private Long userId;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false) private LocalDate dueDate;
 @Column(nullable=false) private LocalDate returnDate;
 @Column(nullable=false) private long overdueDays;
 public Long getId(){return id;} public Long getBorrowId(){return borrowId;} public void setBorrowId(Long v){borrowId=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;} public LocalDate getReturnDate(){return returnDate;} public void setReturnDate(LocalDate v){returnDate=v;} public long getOverdueDays(){return overdueDays;} public void setOverdueDays(long v){overdueDays=v;}
}
