package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import models.login.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.login.LoginSpec.*;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Авторизация и аутентификация")
@Feature("Логин")

public class LoginTests extends TestBase {

    @Test
    @DisplayName("Успешная авторизация пользователя с получением токенов")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    public void successfulLoginTests() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        SuccessfulLoginResponseModel loginResponse = step("Отправка запроса на авторизацию",() ->
                given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfullLoginResponseSpec)
                .extract().as(SuccessfulLoginResponseModel.class)
        );

        step("Проверка полученных токенов", () -> {
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

            assertThat(actualAccess)
                    .as("Access токен должен начинаться с ожидаемого префикса")
                    .startsWith(expectedTokenPath);

            assertThat(actualRefresh)
                    .as("Refresh токен должен начинаться с ожидаемого префикса")
                    .startsWith(expectedTokenPath);

            assertThat(actualAccess)
                    .as("Access и Refresh токены должны быть разными")
                    .isNotEqualTo(actualRefresh);
        });

    }

    @Test
    @DisplayName("Попытка входа с невалидным паролем")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void shouldReturnUnauthorizedWhenPasswordIsInvalid() {

        LoginBodyModel loginData = new LoginBodyModel(username, wrongPassword);

        WrongCredentialsLoginResponseModel loginResponse = step(
                "Отправка запроса на авторизацию с не валидным паролем", () ->
                given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialsLoginResponseSpec)
                .extract().as(WrongCredentialsLoginResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытке входа с невалидным паролем", () -> {
        String actualDetailError = loginResponse.detail();
            assertThat(actualDetailError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedDetailError);
        });
    }

    @Test
    @DisplayName("Попытка входа с невалидным username")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void shouldReturnUnauthorizedWhenUsernameIsInvalid() {

        LoginBodyModel loginData = new LoginBodyModel(wrongUsername, password);

        WrongCredentialsLoginResponseModel loginResponse = step(
                "Отправка запроса на авторизацию с невалидным username", () ->
                given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialsLoginResponseSpec)
                .extract().as(WrongCredentialsLoginResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытке входа с невалидным username", () -> {
            String actualDetailError = loginResponse.detail();
            assertThat(actualDetailError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedDetailError);
        });
    }

    @Test
    @DisplayName("Ошибка при авторизации без логина")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void badRequestLoginWhenUsernameMissingTest() {

        LoginBodyModel loginData = new LoginBodyModel("", password);

        ExistingUserLoginResponseModel loginResponse = step(
                "Отправка запроса на авторизацию с пустым username", () ->
                given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(existingUserLoginResponseSpec)
                .extract().as(ExistingUserLoginResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытке входа без username", () -> {
            String actualError = loginResponse.username().getFirst();
            assertThat(actualError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorPass);
        });
    }

    @Test
    @DisplayName("Ошибка при авторизации без пароля")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void badRequestLoginWhenPasswordMissingTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, "");

        ExistingPasswordLoginResponseModel loginResponse = step(
                "Отправка запроса на авторизацию с пустым паролем", () ->
                given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(existingPasswordLoginResponseSpec)
                .extract().as(ExistingPasswordLoginResponseModel.class)
        );

            step("Проверка сообщения об ошибке при попытке входа с непереданным паролем", () -> {
                String actualError = loginResponse.password().getFirst();
                assertThat(actualError)
                        .as("Сообщение об ошибке должно соответствовать ожидаемому")
                        .isEqualTo(expectedErrorPass);
            });
    }
}
