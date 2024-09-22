package api.common;

import io.restassured.RestAssured;

public class RestAssuredSetUp {
    public static void setUp (){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }
}
