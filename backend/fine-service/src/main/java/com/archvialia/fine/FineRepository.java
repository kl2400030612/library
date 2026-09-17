package com.archvialia.fine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface FineRepository extends JpaRepository<Fine,Long>{ Optional<Fine> findByBorrowId(Long borrowId); List<Fine> findByUserIdOrderByReturnDateDesc(Long userId); }
