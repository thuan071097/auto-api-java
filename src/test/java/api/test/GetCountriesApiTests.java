package api.test;

import api.data.GetCountriesData;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.codehaus.groovy.classgen.asm.BinaryIntExpressionHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.format.ResolverStyle;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.MatcherAssert.assertThat;

public class GetCountriesApiTests {
    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void  VerifyGetCountriesApiResponseSchema () {
        RestAssured.get("/api/v1/countries")
                .then().assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema.json"));
    }

    @Test
    void  VerifyGetCountriesApiReturnCorrectData () {
        String expected = GetCountriesData.ALL_COUNTRIES;
        Response astualResponse = RestAssured.get("/api/v1/countries");
        String actualResponseBody = astualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(IGNORING_ARRAY_ORDER));
    }
}
