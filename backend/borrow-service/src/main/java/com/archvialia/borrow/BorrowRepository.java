package com.archvialia.borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface BorrowRepository extends JpaRepository<BorrowRecord,Long>{List<BorrowRecord> findByUserIdOrderByBorrowDateDesc(Long userId);List<BorrowRecord> findByStatusOrderByDueDateAsc(BorrowStatus status);}
