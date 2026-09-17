package com.archvialia.auth;
import com.archvialia.security.JwtService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuthTokenServiceTest {
 @Test void tokenContainsValidClaims(){JwtService jwt=new JwtService("12345678901234567890123456789012",3600000);String t=jwt.generateToken(1L,"user@example.com","USER");assertTrue(jwt.isValid(t));assertEquals("user@example.com",jwt.parse(t).getSubject());}
}
