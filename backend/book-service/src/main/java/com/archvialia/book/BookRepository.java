package com.archvialia.book;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface BookRepository extends JpaRepository<Book,Long> {
 @Query("select b from Book b where b.active=true and (lower(b.title) like lower(concat('%',:q,'%')) or lower(b.author) like lower(concat('%',:q,'%')) or lower(b.category) like lower(concat('%',:q,'%'))) order by b.title")
 List<Book> search(@Param("q") String q);
}
