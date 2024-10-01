package api.test;

import api.common.DatabaseConnection;
import api.common.LoginUtils;
import api.common.RestAssuredSetUp;
import api.model.User.*;
import api.model.User.dto.DbAddress;
import api.model.User.dto.DbUser;
import api.model.login.LoginInput;
import api.model.login.LoginResponse;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.checkerframework.checker.units.qual.A;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.function.Try;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static api.common.ConstantUtils.*;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsBlankString.blankString;

public class CreateUserApiTests extends MasterTest{

    @Test
    void verifyStaffCreateUserSuccessfullyByDatabase(){
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        // Store the moment before execution
        Instant beforeExecution = Instant.now();
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString()); // Nen dat trc asserThat
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        //Build expected user
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        sessionFactory.inTransaction(session -> {
            DbUser dbUser = session.createSelectionQuery("from DbUser where id=:id", DbUser.class)
                    .setParameter("id",UUID.fromString(actual.getId()))
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
            Instant userCreatedAt = Instant.parse(actualUser.getCreatedAt());
            datetimeVerifier(beforeExecution, userCreatedAt);
            Instant userUpdatedAt = Instant.parse(actualUser.getUpdatedAt());
            datetimeVerifier(beforeExecution, userUpdatedAt);
            actualUser.getAddresses().forEach(actualAddress -> {
                assertThat(actualAddress.getId(), not(blankString()));
                Instant addressCreatedAt = Instant.parse(actualAddress.getCreatedAt());
                datetimeVerifier(beforeExecution, addressCreatedAt);
                Instant addressUpdatedAt = Instant.parse(actualAddress.getCreatedAt());
                datetimeVerifier(beforeExecution, addressUpdatedAt);
            });
        });
    }

    @Test
    void verifyStaffCreateUserSuccessfully(){
        Address address = Address.getDefault();
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address));
        // Store the moment before execution
        Instant beforeExecution = Instant.now();
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString()); // Nen dat trc asserThat
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        Response getCreatedUserResponse = RestAssured.given().log().all()
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actual.getId())
                .get(GET_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));
        // Verify schema
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());

        String actualGetCreated = getCreatedUserResponse.asString();
        assertThat(actualGetCreated, jsonEquals(expectedUser).whenIgnoringPaths("createdAt", "updatedAt",
                "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
        GetUserResponse<AddressResponse> actualGetCreatedModel = getCreatedUserResponse.as(new TypeRef<GetUserResponse<AddressResponse>>() {
        });
        Instant userCreatedAt = Instant.parse(actualGetCreatedModel.getCreatedAt());
        datetimeVerifier(beforeExecution, userCreatedAt);
        Instant userUpdatedAt = Instant.parse(actualGetCreatedModel.getUpdatedAt());
        datetimeVerifier(beforeExecution, userUpdatedAt);
        actualGetCreatedModel.getAddresses().forEach(actualAddress -> {
            assertThat(actualAddress.getId(), not(blankString()));
            Instant addressCreatedAt = Instant.parse(actualAddress.getCreatedAt());
            datetimeVerifier(beforeExecution, addressCreatedAt);
            Instant addressUpdatedAt = Instant.parse(actualAddress.getCreatedAt());
            datetimeVerifier(beforeExecution, addressUpdatedAt);
        });
    }

    @Test
    void verifyStaffCreateUserWithMultipleAddressSuccessfully(){
        Address address1 = Address.getDefault();
        Address address2 = Address.getDefault();
        address2.setStreetNumber("12345");
        User<Address> user = User.getDefault();
        String randomEmail = String.format("auto_api_%s@abc.com", System.currentTimeMillis());
        user.setEmail(randomEmail);
        user.setAddresses(List.of(address1, address2));
        // Store the moment before execution
        Instant beforeExecution = Instant.now();
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString()); // Nen dat trc asserThat
        assertThat(createUserResponse.statusCode(), equalTo(200));
        CreateUserResponse actual = createUserResponse.as(CreateUserResponse.class);
        createdUserIds.add(actual.getId());
        assertThat(actual.getId(), not(blankString()));
        assertThat(actual.getMessage(), equalTo("Customer created"));

        Response getCreatedUserResponse = RestAssured.given().log().all()
                .header(AUTHORIZATION_HEADER, TOKEN)
                .pathParam("id", actual.getId())
                .get(GET_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(getCreatedUserResponse.statusCode(), equalTo(200));
        // Verify schema
        ObjectMapper mapper = new ObjectMapper();
        GetUserResponse<AddressResponse> expectedUser = mapper.convertValue(user, new TypeReference<GetUserResponse<AddressResponse>>() {
        });
        expectedUser.setId(actual.getId());
        expectedUser.getAddresses().get(0).setCustomerId(actual.getId());
        expectedUser.getAddresses().get(1).setCustomerId(actual.getId());

        String actualGetCreated = getCreatedUserResponse.asString();
        assertThat(actualGetCreated, jsonEquals(expectedUser).whenIgnoringPaths("createdAt", "updatedAt",
                "addresses[*].id", "addresses[*].createdAt", "addresses[*].updatedAt"));
        GetUserResponse<AddressResponse> actualGetCreatedModel = getCreatedUserResponse.as(new TypeRef<GetUserResponse<AddressResponse>>() {
        });
        Instant userCreatedAt = Instant.parse(actualGetCreatedModel.getCreatedAt());
        datetimeVerifier(beforeExecution, userCreatedAt);
        Instant userUpdatedAt = Instant.parse(actualGetCreatedModel.getUpdatedAt());
        datetimeVerifier(beforeExecution, userUpdatedAt);
        actualGetCreatedModel.getAddresses().forEach(actualAddress -> {
            assertThat(actualAddress.getId(), not(blankString()));
            Instant addressCreatedAt = Instant.parse(actualAddress.getCreatedAt());
            datetimeVerifier(beforeExecution, addressCreatedAt);
            Instant addressUpdatedAt = Instant.parse(actualAddress.getCreatedAt());
            datetimeVerifier(beforeExecution, addressUpdatedAt);
        });
    }

    static Stream<Arguments> validationUserProvider() throws JsonProcessingException {
        List<Arguments> argumentsList = new ArrayList<>();
        User<Address> user = User.getDefaultWithEmail();

        user.setFirstName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is null", user,
                new ValidationResponse("","must have required property 'firstName'" )));
        user = User.getDefaultWithEmail();
        user.setFirstName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when firstName is empty", user,
                new ValidationResponse("/firstName", "must NOT have fewer than 1 characters")));

        user = User.getDefaultWithEmail();
        user.setLastName(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is null", user,
                new ValidationResponse("","must have required property 'lastName'" )));
        user = User.getDefaultWithEmail();
        user.setLastName("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when lastName is empty", user,
                new ValidationResponse("/lastName", "must NOT have fewer than 1 characters")));

        user = User.getDefaultWithEmail();
        user.setBirthday(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when birthday is null", user,
                new ValidationResponse("","must have required property 'birthday'" )));
        user = User.getDefaultWithEmail();
        user.setBirthday("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when birthday is empty", user,
                new ValidationResponse("/birthday", "must match pattern \"^\\d{2}-\\d{2}-\\d{4}$\"")));

        user = User.getDefaultWithEmail();
        user.setEmail(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when email is null", user,
                new ValidationResponse("","must have required property 'email'" )));
        user = User.getDefaultWithEmail();
        user.setEmail("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when email is empty", user,
                new ValidationResponse("/email", "must match format \"email\"")));

        user = User.getDefaultWithEmail();
        user.setPhone(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone is null", user,
                new ValidationResponse("","must have required property 'phone'"  )));
        user = User.getDefaultWithEmail();
        user.setPhone("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when phone is empty", user,
                new ValidationResponse("/phone", "must match pattern \"^\\d{10,11}$\"")));

        Address address1 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address1));
        address1.setStreetNumber(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when streetNumber is null", user,
                new ValidationResponse("/addresses/0","must have required property 'streetNumber'"  )));
        Address address2 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address2));
        address2.setStreetNumber("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when streetNumber is empty", user,
                new ValidationResponse("/addresses/0/streetNumber", "must NOT have fewer than 1 characters")));

        Address address3 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address3));
        address3.setStreet(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when street is null", user,
                new ValidationResponse("/addresses/0","must have required property 'street'"  )));
        Address address4 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address4));
        address4.setStreet("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when street is empty", user,
                new ValidationResponse("/addresses/0/street", "must NOT have fewer than 1 characters")));

        Address address5 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address5));
        address5.setWard(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when ward is null", user,
                new ValidationResponse("/addresses/0","must have required property 'ward'"  )));
        Address address6 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address6));
        address6.setWard("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when ward is empty", user,
                new ValidationResponse("/addresses/0/ward", "must NOT have fewer than 1 characters")));

        Address address7 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address7));
        address7.setDistrict(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when district is null", user,
                new ValidationResponse("/addresses/0","must have required property 'district'"  )));
        Address address8 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address8));
        address8.setDistrict("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when district is empty", user,
                new ValidationResponse("/addresses/0/district", "must NOT have fewer than 1 characters")));

        Address address9 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address9));
        address9.setCity(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when city is null", user,
                new ValidationResponse("/addresses/0","must have required property 'city'"  )));
        Address address10 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address10));
        address10.setCity("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when city is empty", user,
                new ValidationResponse("/addresses/0/city", "must NOT have fewer than 1 characters")));

        Address address11 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address11));
        address11.setState(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when state is null", user,
                new ValidationResponse("/addresses/0","must have required property 'state'"  )));
        Address address12 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address12));
        address12.setState("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when state is empty", user,
                new ValidationResponse("/addresses/0/state", "must NOT have fewer than 1 characters")));

        Address address13 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address13));
        address13.setZip(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when zip is null", user,
                new ValidationResponse("/addresses/0","must have required property 'zip'"  )));
        Address address14 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address14));
        address14.setZip("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when zip is empty", user,
                new ValidationResponse("/addresses/0/zip", "must match pattern \"^\\d{5}(?:-\\d{4})?$\"")));

        Address address15 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address15));
        address15.setCountry(null);
        argumentsList.add(Arguments.arguments("Verify API return 400 when country is null", user,
                new ValidationResponse("/addresses/0","must have required property 'country'"  )));
        Address address16 = Address.getDefault();
        user = User.getDefaultWithEmail();
        user.setAddresses(List.of(address16));
        address16.setCountry("");
        argumentsList.add(Arguments.arguments("Verify API return 400 when country is empty", user,
                new ValidationResponse("/addresses/0/country", "must NOT have fewer than 2 characters")));

        return argumentsList.stream();
    }

    @ParameterizedTest()
    @MethodSource("validationUserProvider")
    void verifyRequiredFieldsWhenCreatingUser(String testcase, User<Address> user, ValidationResponse expectedResponse){
        Response createUserResponse = RestAssured.given().log().all()
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION_HEADER, TOKEN)
                .body(user)
                .post(CREATE_USER_PATH);
        System.out.printf("Create user response: %s%n", createUserResponse.asString());
        assertThat(createUserResponse.statusCode(), equalTo(400));
        ValidationResponse actual = createUserResponse.as(ValidationResponse.class);
        assertThat(actual, samePropertyValuesAs(expectedResponse));
    }


    private void datetimeVerifier(Instant timeBeforeExecution, Instant actualTime) {
        assertThat(actualTime.isAfter(timeBeforeExecution), equalTo(true));
        assertThat(actualTime.isBefore(Instant.now()), equalTo(true));
    }

}
