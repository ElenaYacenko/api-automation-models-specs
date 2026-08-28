package tests;

import models.registration.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.registration.RegistrationSpec.*;
import static tests.TestData.expectedError;
import static tests.TestData.expectedErrorPass;

public class RegistrationTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    public void prepareTestData() {
        Faker faker = new Faker();
        long timestamp = System.currentTimeMillis();
        username = faker.name().firstName() + timestamp;
        password = faker.name().firstName() + timestamp;
    }

    @Test
    public void successfulRegistrationTests() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel registrationResponse = given(baseRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        assertThat(registrationResponse.username()).isEqualTo(username);
        assertThat(registrationResponse.id()).isInstanceOf(Integer.class);
        assertThat(registrationResponse.firstName()).isEqualTo("");
        assertThat(registrationResponse.lastName()).isEqualTo("");
        assertThat(registrationResponse.email()).isEqualTo("");
    }

    @Test
    public void existingUserTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel registrationResponse = given(baseRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        assertThat(registrationResponse.username()).isEqualTo(username);

        ExistingUserResponseModel response = given(baseRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(existingUserResponseSpec)
                .extract()
                .as(ExistingUserResponseModel.class);

        String actualError = response.username().getFirst();
        assertThat(actualError).isEqualTo(expectedError);
    }

    @Test
    public void badRequestWhenUsernameMissingTest() {

        ExistingUserResponseModel response = given(baseRequestSpec)
                .body(new RegistrationBodyModel("", password))
                .when()
                .post("/users/register/")
                .then()
                .spec(existingUserResponseSpec)
                .extract()
                .as(ExistingUserResponseModel.class);

        String actualError = response.username().getFirst();
        assertThat(actualError).isEqualTo(expectedErrorPass);
    }

    @Test
    public void badRequestWhenPasswordMissingTest() {

        ExistingPasswordResponseModel response = given(baseRequestSpec)
                .body(new RegistrationBodyModel(username, ""))
                .when()
                .post("/users/register/")
                .then()
                .spec(existingPasswordResponseSpec)
                .extract()
                .as(ExistingPasswordResponseModel.class);

        String actualError = response.password().getFirst();
        assertThat(actualError).isEqualTo(expectedErrorPass);
    }

    @Test
    public void badRequestWhenBothFieldsMissingTest() {

        ExistingNoParamResponseModel response = given(baseRequestSpec)
                .body(new RegistrationBodyModel("", ""))
                .when()
                .post("/users/register/")
                .then()
                .spec(existingMissingResponseSpec)
                .extract()
                .as(ExistingNoParamResponseModel.class);

        String actualErrorPass = response.password().getFirst();
        String actualErrorName = response.username().getFirst();
        assertThat(actualErrorName).isEqualTo(expectedErrorPass);
        assertThat(actualErrorPass).isEqualTo(expectedErrorPass);
    }

}
