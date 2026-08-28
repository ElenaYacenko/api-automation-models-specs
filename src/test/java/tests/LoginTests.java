package tests;

import models.login.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.login.LoginSpec.*;
import static tests.TestData.*;

public class LoginTests extends TestBase {

    @Test
    public void successfulLoginTests() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        SuccessfulLoginResponseModel loginResponse = given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfullLoginResponseSpec)
                .extract().as(SuccessfulLoginResponseModel.class);

        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        assertThat(actualAccess).startsWith(expectedTokenPath);
        assertThat(actualRefresh).startsWith(expectedTokenPath);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);
    }

    @Test
    public void shouldReturnUnauthorizedWhenPasswordIsInvalid() {

        LoginBodyModel loginData = new LoginBodyModel(username, wrongPassword);

        WrongCredentialsLoginResponseModel loginResponse = given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialsLoginResponseSpec)
                .extract().as(WrongCredentialsLoginResponseModel.class);

        String actualDetailError = loginResponse.detail();

        assertThat(actualDetailError).isEqualTo(expectedDetailError);
    }

    @Test
    @DisplayName("Успешная авторизация")
    public void shouldReturnUnauthorizedWhenUsernameIsInvalid() {

        LoginBodyModel loginData = new LoginBodyModel(wrongUsername, password);

        WrongCredentialsLoginResponseModel loginResponse = given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialsLoginResponseSpec)
                .extract().as(WrongCredentialsLoginResponseModel.class);

        String actualDetailError = loginResponse.detail();

        assertThat(actualDetailError).isEqualTo(expectedDetailError);
    }

    @Test
    @DisplayName("Ошибка при авторизации если не передавать логин")
    public void badRequestLoginWhenUsernameMissingTest() {

        LoginBodyModel loginData = new LoginBodyModel("", password);

        ExistingUserLoginResponseModel loginResponse = given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(existingUserLoginResponseSpec)
                .extract().as(ExistingUserLoginResponseModel.class);

        String actualError = loginResponse.username().getFirst();
        assertThat(actualError).isEqualTo(expectedErrorPass);
    }

    @Test
    @DisplayName("Ошибка при авторизации если не передавать пароль")
    public void badRequestLoginWhenPasswordMissingTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, "");

        ExistingPasswordLoginResponseModel loginResponse = given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(existingPasswordLoginResponseSpec)
                .extract().as(ExistingPasswordLoginResponseModel.class);

        String actualError = loginResponse.password().getFirst();
        assertThat(actualError).isEqualTo(expectedErrorPass);
    }
}
