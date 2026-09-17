package com.archvialia.borrow;

import com.archvialia.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/borrow")
public class BorrowController {
 private final BorrowRepository repo; private final ServiceClients clients; private final int loanDays;
 public BorrowController(BorrowRepository repo,ServiceClients clients,@Value("${app.loan-days:14}") int loanDays){this.repo=repo;this.clients=clients;this.loanDays=loanDays;}
 @PostMapping public BorrowResponse borrow(@Valid @RequestBody BorrowRequest req,HttpServletRequest request){
   var user=SecurityUtils.currentUser(); String auth=request.getHeader(HttpHeaders.AUTHORIZATION);
   BorrowRecord r=new BorrowRecord();r.setUserId(user.id());r.setBookId(req.bookId());r.setBorrowDate(LocalDate.now());r.setDueDate(LocalDate.now().plusDays(loanDays));
   try { clients.reserveBook(req.bookId(),auth); } catch(Exception e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Book is unavailable or Book Service is unreachable",e); }
   try { return BorrowResponse.from(repo.save(r)); } catch(Exception e){ try{clients.releaseBook(req.bookId(),auth);}catch(Exception ignored){} throw e; }
 }
 @GetMapping("/me") public List<BorrowResponse> mine(){return repo.findByUserIdOrderByBorrowDateDesc(SecurityUtils.currentUser().id()).stream().map(BorrowResponse::from).toList();}
 @GetMapping("/{id}") public BorrowResponse get(@PathVariable Long id){BorrowRecord r=find(id);if(!r.getUserId().equals(SecurityUtils.currentUser().id())&&!isAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Forbidden");return BorrowResponse.from(r);}
 @PutMapping("/{id}/return") public BorrowResponse returnBook(@PathVariable Long id,HttpServletRequest request){
   BorrowRecord r=find(id);var user=SecurityUtils.currentUser();if(!r.getUserId().equals(user.id())&&!isAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Forbidden");if(r.getStatus()==BorrowStatus.RETURNED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Borrow record already returned");
   String auth=request.getHeader(HttpHeaders.AUTHORIZATION);LocalDate today=LocalDate.now();
   try{clients.releaseBook(r.getBookId(),auth);}catch(Exception e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Book Service is unavailable",e);}
   r.setReturnDate(today);r.setStatus(BorrowStatus.RETURNED);r=repo.save(r);
   try{clients.calculateFine(r.getId(),r.getUserId(),r.getDueDate(),today,auth);}catch(Exception ignored){ /* return remains successful; fine can be calculated/retried */ }
   return BorrowResponse.from(r);
 }
 @GetMapping("/admin/all") @PreAuthorize("hasRole('ADMIN')") public List<BorrowResponse> all(){return repo.findAll().stream().map(BorrowResponse::from).toList();}
 @GetMapping("/health") public String health(){return "borrow-service:UP";}
 private BorrowRecord find(Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Borrow record not found"));}
 private boolean isAdmin(){return SecurityUtils.currentUser().role().equals("ADMIN");}
 public record BorrowRequest(@NotNull Long bookId){}
 public record BorrowResponse(Long id,Long userId,Long bookId,LocalDate borrowDate,LocalDate dueDate,LocalDate returnDate,BorrowStatus status){static BorrowResponse from(BorrowRecord r){return new BorrowResponse(r.getId(),r.getUserId(),r.getBookId(),r.getBorrowDate(),r.getDueDate(),r.getReturnDate(),r.getStatus());}}
}
