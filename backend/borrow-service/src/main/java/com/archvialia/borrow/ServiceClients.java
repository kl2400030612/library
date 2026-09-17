package com.archvialia.borrow;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ServiceClients {
    private final WebClient bookClient;
    private final WebClient fineClient;
    public ServiceClients(WebClient.Builder builder){
        this.bookClient=builder.baseUrl("http://BOOK-SERVICE").build();
        this.fineClient=builder.baseUrl("http://FINE-SERVICE").build();
    }
    public void reserveBook(Long bookId,String auth){
        bookClient.post().uri("/api/books/{id}/reserve",bookId).header(HttpHeaders.AUTHORIZATION,auth).retrieve().toBodilessEntity().block();
    }
    public void releaseBook(Long bookId,String auth){
        bookClient.post().uri("/api/books/{id}/release",bookId).header(HttpHeaders.AUTHORIZATION,auth).retrieve().toBodilessEntity().block();
    }
    public void calculateFine(Long borrowId,Long userId,java.time.LocalDate dueDate,java.time.LocalDate returnDate,String auth){
        fineClient.post().uri("/api/fines/calculate/{id}",borrowId).header(HttpHeaders.AUTHORIZATION,auth).bodyValue(new FineRequest(userId,dueDate,returnDate)).retrieve().toBodilessEntity().block();
    }
    public record FineRequest(Long userId,java.time.LocalDate dueDate,java.time.LocalDate returnDate){}
}
