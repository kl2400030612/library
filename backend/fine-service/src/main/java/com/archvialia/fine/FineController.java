package com.archvialia.fine;

import com.archvialia.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController @RequestMapping("/api/fines")
public class FineController {
 private final FineRepository repo; private final BigDecimal dailyRate;
 public FineController(FineRepository repo,@Value("${app.fine.daily-rate:10.00}") BigDecimal dailyRate){this.repo=repo;this.dailyRate=dailyRate;}
 @GetMapping("/me") public List<FineResponse> mine(){Long uid=SecurityUtils.currentUser().id();return repo.findByUserIdOrderByReturnDateDesc(uid).stream().map(FineResponse::from).toList();}
 @GetMapping("/{borrowId}") public FineResponse byBorrow(@PathVariable Long borrowId){Fine f=repo.findByBorrowId(borrowId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Fine not found")); if(!f.getUserId().equals(SecurityUtils.currentUser().id()) && !isAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Forbidden"); return FineResponse.from(f);}
 @PostMapping("/calculate/{borrowId}") public FineResponse calculate(@PathVariable Long borrowId,@Valid @RequestBody FineRequest req){ if(!req.userId().equals(SecurityUtils.currentUser().id()) && !isAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Forbidden"); return calculateInternal(borrowId,req); }
 @GetMapping("/admin/all") @PreAuthorize("hasRole('ADMIN')") public List<FineResponse> all(){return repo.findAll().stream().map(FineResponse::from).toList();}
 @GetMapping("/health") public String health(){return "fine-service:UP";}
 private FineResponse calculateInternal(Long borrowId,FineRequest req){
   long overdue=Math.max(0,ChronoUnit.DAYS.between(req.dueDate(),req.returnDate()));
   BigDecimal amount=dailyRate.multiply(BigDecimal.valueOf(overdue));
   Fine f=repo.findByBorrowId(borrowId).orElseGet(Fine::new); f.setBorrowId(borrowId);f.setUserId(req.userId());f.setDueDate(req.dueDate());f.setReturnDate(req.returnDate());f.setOverdueDays(overdue);f.setAmount(amount); return FineResponse.from(repo.save(f));
 }
 private boolean isAdmin(){return SecurityUtils.currentUser().role().equals("ADMIN");}
 public record FineRequest(@NotNull Long userId,@NotNull LocalDate dueDate,@NotNull LocalDate returnDate){}
 public record FineResponse(Long id,Long borrowId,Long userId,BigDecimal amount,LocalDate dueDate,LocalDate returnDate,long overdueDays){static FineResponse from(Fine f){return new FineResponse(f.getId(),f.getBorrowId(),f.getUserId(),f.getAmount(),f.getDueDate(),f.getReturnDate(),f.getOverdueDays());}}
}
