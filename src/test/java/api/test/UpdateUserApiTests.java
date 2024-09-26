package api.test;

import api.common.DatabaseConnection;
import api.common.LoginUtils;
import api.common.RestAssuredSetUp;
import api.model.User.*;
import api.model.User.dto.DbAddress;
import api.model.User.dto.DbUser;
import api.model.login.LoginResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static api.common.ConstantUtils.*;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankString;

public class UpdateUserApiTests {
    private static List<String> createdUserIds = new ArrayList<>();
    private static List<String> updatedUserIds = new ArrayList<>();
    private static String TOKEN = "";
    private static long TIMEOUT = -1;
    private static long TIME_BEFORE_GET_TOKEN = -1;
    private static SessionFactory sessionFactory = DatabaseConnection.getSession();

    @BeforeAll
    static void setUp(){
        RestAssuredSetUp.setUp();
    }

    @BeforeEach
    void beforeEach(){
        if (TIMEOUT==-1 || (System.currentTimeMillis() - TIME_BEFORE_GET_TOKEN) > TIMEOUT * 0.8){
            //Get token
            TIME_BEFORE_GET_TOKEN = System.currentTimeMillis();
            LoginResponse loginResponse = LoginUtils.login();
            assertThat(loginResponse.getToken(), not(blankString()));
            TOKEN = "Bearer ".concat(loginResponse.getToken());
            TIMEOUT = loginResponse.getTimeout();
        }
    }

    @Test
    void verifyUpdateUserSuccessfully(){
        //Tao moi user
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        Instant beforeExecution = Instant.now();

        // Gui yeu cau tao user
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());

        // Xac thuc user da duoc tao thanh cong
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        // Cap nhat thong tin user
        String updatedEmail = String.format("update_auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(updatedEmail);
        user.setAddresses(List.of(address));

        // Gui yeu cau cap nhat user
        Response updateUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actual.getId())
                .body(user)
                .put(UPDATE_USER_PATH);
        System.out.printf("Update user response: %s%n", updateUserResponse.asString()); // Nen dat trc asserThat
        assertThat(updateUserResponse.statusCode(), equalTo(200));
        UpdateUserResponse actualResult = updateUserResponse.as(UpdateUserResponse.class);
        updatedUserIds.add(actualResult.getId());
        assertThat(actualResult.getId(), not(blankString()));
        assertThat(actualResult.getMessage(), equalTo("Customer updated"));

        // Lay lai thong tin user vua cap nhat
        Response getUpdatedUserResponse = RestAssured.given().log().all()
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actualResult.getId())
                .get(GET_USER_PATH);
        System.out.printf("Update user response: %s%n", updateUserResponse.asString());
        assertThat(getUpdatedUserResponse.statusCode(), equalTo(200));

        // Xac thuc thong tin user vua duoc cap nhat
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        GetUserResponse<AddressResponse> actualUpdatedUser = getUpdatedUserResponse.as(new TypeRef<GetUserResponse<AddressResponse>>(){});
        expectedUser.setId(actualUpdatedUser.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actualUpdatedUser.getId());
        assertThat(actualUpdatedUser.getEmail(), equalTo(updatedEmail));

        // Kiem tra thoi gian cap nhat
        String actualGetUpdated = getUpdatedUserResponse.asString();
        assertThat(actualGetUpdated, jsonEquals(expectedUser).whenIgnoringPaths("createdAt", "updatedAt",
                "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
        GetUserResponse<AddressResponse> actualGetUpdatedModel = getUpdatedUserResponse.as(new TypeRef<GetUserResponse<AddressResponse>>() {
        });
        Instant userUpdatedAt = Instant.parse(actualGetUpdatedModel.getUpdatedAt());
        datetimeVerifier(beforeExecution, userUpdatedAt);
        actualGetUpdatedModel.getAddresses().forEach(actualAddress -> {
            assertThat(actualAddress.getId(), not(blankString()));
            Instant addressUpdatedAt = Instant.parse(actualAddress.getUpdatedAt());
            datetimeVerifier(beforeExecution, addressUpdatedAt);
        });
    }

    @Test
    void verifyUpdateUserSuccessfullyByDatabase(){
        //Tao moi user
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        Instant beforeExecution = Instant.now();

        // Gui yeu cau tao user
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());

        // Xac thuc user da duoc tao thanh cong
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        // Cap nhat thong tin user
        String updatedEmail = String.format("update_auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(updatedEmail);
        user.setAddresses(List.of(address));

        // Gui yeu cau cap nhat user
        Response updateUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actual.getId())
                .body(user)
                .put(UPDATE_USER_PATH);
        System.out.printf("Update user response: %s%n", updateUserResponse.asString()); // Nen dat trc asserThat
        assertThat(updateUserResponse.statusCode(), equalTo(200));
        UpdateUserResponse actualResult = updateUserResponse.as(UpdateUserResponse.class);
        updatedUserIds.add(actualResult.getId());
        assertThat(actualResult.getId(), not(blankString()));
        assertThat(actualResult.getMessage(), equalTo("Customer updated"));

        //Build expected user
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        expectedUser.setId(actualResult.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actualResult.getId());

        sessionFactory.inTransaction(session -> {
            DbUser dbUser = session.createSelectionQuery("from DbUser where id=:id", DbUser.class)
                    .setParameter("id", UUID.fromString(actual.getId()))
                    .getSingleResult();
            List<DbAddress> dbAddresses = session.createSelectionQuery("from DbAddress where customerId=:customerId", DbAddress.class)
                    .setParameter("customerId", UUID.fromString(actual.getId()))
                    .getResultList();
            GetUserResponse<AddressResponse> actualUser = mapper.convertValue(dbUser, new TypeReference<GetUserResponse<AddressResponse>>() {
            });
            actualUser.setAddresses(mapper.convertValue(dbAddresses, new TypeReference<List<AddressResponse>>(){
            }));
            assertThat(actualUser, jsonEquals(expectedUser).whenIgnoringPaths("createdAt", "updatedAt",
                    "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
            Instant userUpdatedAt = Instant.parse(actualUser.getUpdatedAt());
            datetimeVerifier(beforeExecution, userUpdatedAt);
            actualUser.getAddresses().forEach(actualAddress -> {
                assertThat(actualAddress.getId(), not(blankString()));
                Instant addressUpdatedAt = Instant.parse(actualAddress.getUpdatedAt());
                datetimeVerifier(beforeExecution, addressUpdatedAt);
            });
        });
    }

    private void datetimeVerifier(Instant timeBeforeExecution, Instant actualTime) {
        assertThat(actualTime.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(actualTime.isBefore(Instant.now()), equalTo(true));
    }

    @AfterAll
    static void tearDown(){
        createdUserIds.forEach(id -> {
            RestAssured.given().log().all()
                    .header(AUTHORIZATION_HEADER, TOKEN)
                    .pathParam("id", id)
                    .delete(DELETE_USER_PATH);
        });
        sessionFactory.close();
    }
}
