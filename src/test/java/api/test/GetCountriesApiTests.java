package api.test;

import api.data.GetCountriesData;
import api.model.country.Country;
import api.model.country.CountryVersionTwo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import jdk.javadoc.doclet.Reporter;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.classgen.asm.BinaryIntExpressionHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GetCountriesApiTests {

    private static final String GET_COUNTRIES_PATH = "/api/v1/countries";
    private static final String GET_COUNTRIES_V2_PATH = "/api/v2/countries";
    private static final String GET_COUNTRY_BY_CODE_PATH = "/api/v1/countries/{code}";
    private static final String GET_COUNTRY_BY_FILTER = "/api/v3/countries";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void  VerifyGetCountriesApiResponseSchema () {
        RestAssured.get(GET_COUNTRIES_PATH)
                .then().assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-json-schema.json"));
    }

    @Test
    void  VerifyGetCountriesApiReturnCorrectData () {
        String expected = GetCountriesData.ALL_COUNTRIES;
        Response astualResponse = RestAssured.get(GET_COUNTRIES_PATH);
        String actualResponseBody = astualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(IGNORING_ARRAY_ORDER));
    }
    @Test
    void  VerifyGetCountriesApiV2ResponseSchema () {
        RestAssured.get(GET_COUNTRIES_V2_PATH)
                .then().assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-countries-v2-json-schema.json"));
    }

    @Test
    void  VerifyGetCountriesApiV2ReturnCorrectData () {
        String expected = GetCountriesData.ALL_COUNTRIES_V2;
        Response astualResponse = RestAssured.get(GET_COUNTRIES_V2_PATH);
        String actualResponseBody = astualResponse.asString();
        assertThat(actualResponseBody, jsonEquals(expected).when(IGNORING_ARRAY_ORDER));
    }

    @Test
    void  VerifyGetCountryByCodeResponseSchema () {
        Map<String, String> params = new HashMap<>();
        params.put("code", "VN");
        RestAssured.get(GET_COUNTRY_BY_CODE_PATH,params)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/get-country-by-code-json-schema.json"));
    }

    static Stream<Country> countriesProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries = mapper.<List<Country>>readValue(GetCountriesData.ALL_COUNTRIES, new TypeReference<List<Country>>() {
        });
        return countries.stream();
    }

    @ParameterizedTest
    @MethodSource("countriesProvider")
    void  VerifyGetCountryByCodeApiReturnCorrectData (Country country) {
            Map<String, String> params = new HashMap<>();
            params.put("code", country.getCode());
            Response actualResponse = RestAssured.given().log().all()
                    .get(GET_COUNTRY_BY_CODE_PATH, params);
            assertThat(200, equalTo(actualResponse.statusCode()));
            String actualResponseBody = actualResponse.asString();
            assertThat(String.format("Actual: %s\n Expected: %s\n", actualResponseBody, country), actualResponseBody, jsonEquals(country));
    }

    @Test
    void verifyGetCountriesApiReturnCorrectDataWhenGivenLessThanOperator(){
        String path = String.format("%s?gdp=5000&operator=<", GET_COUNTRY_BY_FILTER);
        Response actualResponse = RestAssured.given().log().all()
                .get(path);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
            });
        countries.forEach(country -> assertThat(country.getGdp(), lessThan(5000f)));
    }

    @Test
    void verifyGetCountriesApiReturnCorrectDataWhenGivenGreaterThanOperator(){
        String path = String.format("%s?gdp=5000&operator=>", GET_COUNTRY_BY_FILTER);
        Response actualResponse = RestAssured.given().log().all()
                .get(path);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
        });
        countries.forEach(country -> assertThat(country.getGdp(), greaterThan(5000f)));
    }

    @Test
    void verifyGetCountriesApiReturnCorrectDataWhenGivenEqualOperator() {
        Response actualResponse = RestAssured.given().log().all()
                .queryParam("gdp", "5000")
                .queryParam("operator", "==")
                .get(GET_COUNTRY_BY_FILTER);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
        });
        countries.forEach(country -> assertThat(country.getGdp(), equalTo(5000f)));
    }

    @Test
    void verifyGetCountriesApiReturnCorrectDataWhenGivenLessThanOrEqualOperator(){
        String path = String.format("%s?gdp=5000&operator=<=", GET_COUNTRY_BY_FILTER);
        Response actualResponse = RestAssured.given().log().all()
                .get(path);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
        });
        countries.forEach(country -> assertThat(country.getGdp(), lessThanOrEqualTo(5000f)));
    }

    @Test
    void verifyGetCountriesApiReturnCorrectDataWhenGivenGreaterThanOrEqualOperator(){
        String path = String.format("%s?gdp=5000&operator=>=", GET_COUNTRY_BY_FILTER);
        Response actualResponse = RestAssured.given().log().all()
                .get(path);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
        });
        countries.forEach(country -> assertThat(country.getGdp(), greaterThanOrEqualTo(5000f)));
    }

    @Test
    void verifyGetCountriesApiReturnCorrectDataWhenGivenGreaterNotEqualOperator(){
        String path = String.format("%s?gdp=5000&operator=!=", GET_COUNTRY_BY_FILTER);
        Response actualResponse = RestAssured.given().log().all()
                .get(path);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<CountryVersionTwo> countries = actualResponse.as(new TypeRef<List<CountryVersionTwo>>() {
        });
        countries.forEach(country -> assertThat(country.getGdp(), not(equalTo(5000f))));
    }
}
