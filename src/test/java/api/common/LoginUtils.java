package api.common;

import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LoginUtils {
    public static final String LOGIN_PATH = "/api/login";

    public static LoginResponse login(){
        return login("staff", "1234567890");
    }

    public static LoginResponse login(String username, String password){
        LoginInput loginInput = new LoginInput(username, password);
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        return actualResponse.as(LoginResponse.class);
    }
}
