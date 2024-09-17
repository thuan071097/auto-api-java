package api.test;

import api.model.country.Country;
import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.text.IsBlankString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ObjectInput;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartMatches;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.text.IsBlankString.blankString;

public class LoginApiTests {
    private static final String LOGIN_PATH = "/api/login";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifyStaffLoginSuccessfully(){
        LoginInput loginInput = new LoginInput("staff", "1234567890");
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        assertThat(actualResponse.statusCode(), equalTo(200));

        //Need to verify schema
        actualResponse.then().assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/post-login-json-schema.json"));

        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getToken(), not(blankString()));
        assertThat(loginResponse.getTimeout(), equalTo(120000));
    }

    static Stream<Arguments> loginProvider(){
        return Stream.of(
                Arguments.of(new LoginInput("staff1", "1234567890")
                        , 401, "Invalid credentials"), // Invalid username, Valid password
                Arguments.of(new LoginInput("staff", "12345678901")
                        , 401, "Invalid credentials"), // Valid username, Invalid password
                Arguments.of(new LoginInput("staff2", "12345678901")
                        , 401, "Invalid credentials"), //Invalid username, Invalid password
                Arguments.of(new LoginInput(null, "1234567890")
                        , 401, "Invalid credentials"), //Required field - null
                Arguments.of(new LoginInput("staff", "")
                        , 401, "Invalid credentials") //Required field - empty
        );
    }

    @ParameterizedTest
    @MethodSource("loginProvider")
    void verifyLoginUnhappyCases(LoginInput loginInput, int expectedStatusCode, String expectedErrorMessage) {
        Response actualResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .body(loginInput)
                .post(LOGIN_PATH);
        assertThat(actualResponse.statusCode(), equalTo(expectedStatusCode));
        LoginResponse loginResponse = actualResponse.as(LoginResponse.class);
        assertThat(loginResponse.getMessage(), equalTo(expectedErrorMessage));
    }

}
