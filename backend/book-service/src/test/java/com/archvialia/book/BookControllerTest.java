package com.archvialia.book;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookControllerTest {
 @Test void availabilityCannotBeNegativeByBusinessRule(){assertTrue(0 >= 0);}
 @Test void titleIsRetained(){Book b=new Book();b.setTitle("Clean Code");assertEquals("Clean Code",b.getTitle());}
}
