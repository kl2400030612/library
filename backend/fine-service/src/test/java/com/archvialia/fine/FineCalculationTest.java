package com.archvialia.fine;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;
class FineCalculationTest {
 @Test void noFineWhenReturnedOnDueDate(){assertEquals(0,ChronoUnit.DAYS.between(LocalDate.of(2026,9,16),LocalDate.of(2026,9,16)));}
 @Test void overdueDaysCalculated(){assertEquals(3,ChronoUnit.DAYS.between(LocalDate.of(2026,9,16),LocalDate.of(2026,9,19)));}
}
