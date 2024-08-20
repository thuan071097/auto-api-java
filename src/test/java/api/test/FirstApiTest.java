package api.test;

import org.junit.jupiter.api.Test;

public class FirstApiTest {
    @Test
    void testGetApi (){
        System.out.println("Hello");
        String a = "Tho";
        a = a.concat("Test");
        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
    }
}
