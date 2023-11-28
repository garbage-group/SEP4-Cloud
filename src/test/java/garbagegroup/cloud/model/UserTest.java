package garbagegroup.cloud.model;

import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import static org.junit.jupiter.api.Assertions.*;

import javax.validation.constraints.Size;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    private String usernameTest;
    private String passwordTest;
    private String fullnameTest;
    private String roleTest;
    private String regionTest;
    private User userTest;

    @BeforeEach
    public void setUp(){
        usernameTest= "MunicipalityWorker";
        passwordTest="Password";
        fullnameTest="Municipality worker";
        roleTest="municipality_worker";
        regionTest="Horsens";
        userTest= new User(usernameTest,passwordTest,fullnameTest,roleTest,regionTest);
    }

    @Test
    public void ctorTest(){
        assertEquals(userTest.getUsername(),usernameTest);
        assertEquals(userTest.getFullname(),fullnameTest);
        assertEquals(userTest.getPassword(),passwordTest);
        assertEquals(userTest.getRole(),roleTest);
        assertEquals(userTest.getRegion(),regionTest);

    }

    @Test
    public void checkPassword_notNullable_resultTrue() {
        assertNotNull(userTest.getPassword(), "Password should not be null");
    }

    @Test
    public void checkFullname_notNullable_resultTrue() {
        assertNotNull(userTest.getFullname(), "Fullname should not be null");
    }


    @Test
    public void checkPassword_between_8and10character_resultTrue() {
        String password = userTest.getPassword();
        int passwordLength = password.length();
        assertTrue(passwordLength >= 8 && passwordLength <= 10,
                "Password should be between 8 and 10 characters");
    }

    @Test
    public void checkPassword_lengthValidation() {
        String password = userTest.getPassword();
        int passwordLength = password.length();

        boolean isBetween8And10Characters = passwordLength >= 8 && passwordLength <= 10;

        assertTrue(isBetween8And10Characters,
                "Password should be between 8 and 10 characters");

        boolean isLessThan8Characters = passwordLength < 8;
        boolean isMoreThan10Characters = passwordLength > 10;

        assertFalse(isLessThan8Characters,
                "Password should not be less than 8 characters");
        assertFalse(isMoreThan10Characters,
                "Password should not be more than 10 characters");
    }


}
