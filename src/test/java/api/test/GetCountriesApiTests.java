package api.test;

import api.data.GetCountriesData;
import api.model.country.Country;
import api.model.country.CountryPagination;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static api.data.GetCountriesData.ALL_COUNTRIES_V3;
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
    private static final String GET_COUNTRY_PAGINATION = "/api/v4/countries";
    private static final String GET_COUNTRY_PRIVATE = "/api/v5/countries";

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

//    @Test
//    void  VerifyGetCountriesApiV3ReturnCorrectData () {
//        String expected = ALL_COUNTRIES_V3;
//        Response astualResponse = RestAssured.get(GET_COUNTRY_PRIVATE);
//        String actualResponseBody = astualResponse.asString();
//        assertThat(actualResponseBody, jsonEquals(expected).when(IGNORING_ARRAY_ORDER));
//    }

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
            Country actualResponseBody = actualResponse.as(Country.class);
            assertThat(String.format("Actual: %s\n Expected: %s\n", actualResponseBody, country), actualResponseBody, jsonEquals(country));
    }

    static Stream<Map<String, String>> getCountriesByFilterProvider() throws JsonProcessingException {
        List<Map<String, String>> inputs = new ArrayList<>();
        inputs.add(Map.of("gdp", "5000", "operator", ">"));
        inputs.add(Map.of("gdp", "5000", "operator", "<"));
        inputs.add(Map.of("gdp", "5000", "operator", ">="));
        inputs.add(Map.of("gdp", "5000", "operator", "<="));
        inputs.add(Map.of("gdp", "5000", "operator", "=="));
        inputs.add(Map.of("gdp", "5000", "operator", "!="));
        return inputs.stream();
    }

    @ParameterizedTest
    @MethodSource("getCountriesByFilterProvider")
    void verifyGetCountriesApiReturnCorrectDataWithCorrespondingFilter(Map<String, String> queryParams) {
        Response actualResponse = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get(GET_COUNTRY_BY_FILTER);
        assertThat(200, equalTo(actualResponse.statusCode()));
        List<Country> countries = actualResponse.as(new TypeRef<List<Country>>() {
        });
        countries.forEach(country -> {
            float actualGdp = Float.parseFloat(queryParams.get("gdp"));
            Matcher<Float> marcher = switch (queryParams.get("operator")){
                case ">" -> greaterThan(actualGdp);
                case "<" -> lessThan(actualGdp);
                case "<=" -> lessThanOrEqualTo(actualGdp);
                case ">=" -> greaterThanOrEqualTo(actualGdp);
                case "==" -> equalTo(actualGdp);
                case "!=" -> not(equalTo(actualGdp));
                default -> equalTo(actualGdp);
            };
            assertThat(country.getGdp(), marcher);
        });
    }

    private CountryPagination getCountryPagination(int page, int size) {
        Response actualResponseFirstPage = RestAssured.given().log().all()
                .queryParam("page", page)
                .queryParam("size", size)
                .get(GET_COUNTRY_PAGINATION);
        return actualResponseFirstPage.as(new TypeRef<CountryPagination>() {
        });
    }

    @Test
    void verifyGetCountriesPagination(){
        int pageSize = 2;
        CountryPagination countryPaginationFirstPage = getCountryPagination(1, pageSize);
        CountryPagination countryPaginationSecondPage = getCountryPagination(2, pageSize);

        assertThat(countryPaginationFirstPage.getData().size(), equalTo(pageSize));
        assertThat(countryPaginationSecondPage.getData().size(), equalTo(pageSize));
        assertThat(countryPaginationFirstPage.getData().containsAll(countryPaginationSecondPage.getData()), is(false));

        int sizeOfLastPage = countryPaginationFirstPage.getTotal() % pageSize;
        int lastPage = countryPaginationFirstPage.getTotal() / pageSize;
        if (sizeOfLastPage > 0){
            lastPage++;
        }
        if (sizeOfLastPage == 0){
            sizeOfLastPage = pageSize;
        }
        CountryPagination countryPaginationLastPage = getCountryPagination(lastPage, pageSize);
        assertThat(countryPaginationLastPage.getData().size(), equalTo(sizeOfLastPage));
        CountryPagination countryPaginationLastPagePlus = getCountryPagination(lastPage + 1, pageSize);
        assertThat(countryPaginationLastPagePlus.getData().size(), equalTo(0));
    }

    @Test
    void verifyGetCountriesWithPrivateKey (){
        Response actualResponse = RestAssured.given().log() .all()
                .header("api-key","private")
                .get(GET_COUNTRY_PRIVATE);
        List<Country> countries = actualResponse.as(new TypeRef<List<Country>>() {
        });
        assertThat(actualResponse.asString(), jsonEquals(ALL_COUNTRIES_V3).when(IGNORING_ARRAY_ORDER));
    }
}
