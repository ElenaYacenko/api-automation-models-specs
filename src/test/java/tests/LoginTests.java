package tests;

import io.qameta.allure.*;
import models.login.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Авторизация и аутентификация")
@Feature("Авторизация")
public class LoginTests extends TestBase {

    @Test
    @Description("POST /auth/token/ с валидными учётными данными: access и refresh начинаются с ожидаемого префикса и не совпадают друг с другом")
    @DisplayName("Успешная авторизация пользователя с получением токенов")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void successfulLoginTests() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);

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
    @Description("POST /auth/token/ с невалидным паролем: возвращается ошибка с детализацией")
    @DisplayName("Попытка входа с невалидным паролем")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturnUnauthorizedWhenPasswordIsInvalid() {

        LoginBodyModel loginData = new LoginBodyModel(username, wrongPassword);

        WrongCredentialsLoginResponseModel loginResponse = api.auth.loginWrongCredentials(loginData);

        step("Проверка сообщения об ошибке при попытке входа с невалидным паролем", () -> {
            String actualDetailError = loginResponse.detail();
            assertThat(actualDetailError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedDetailError);
        });
    }

    @Test
    @Description("POST /auth/token/ с невалидным username: возвращается ошибка с детализацией")
    @DisplayName("Попытка входа с невалидным username")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturnUnauthorizedWhenUsernameIsInvalid() {

        LoginBodyModel loginData = new LoginBodyModel(wrongUsername, password);

        WrongCredentialsLoginResponseModel loginResponse = api.auth.loginWrongCredentials(loginData);

        step("Проверка сообщения об ошибке при попытке входа с невалидным username", () -> {
            String actualDetailError = loginResponse.detail();
            assertThat(actualDetailError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedDetailError);
        });
    }

    @Test
    @Description("POST /auth/token/ с пустым username: возвращается ошибка валидации")
    @DisplayName("Ошибка при авторизации без логина")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void badRequestLoginWhenUsernameMissingTest() {

        LoginBodyModel loginData = new LoginBodyModel("", password);

        ExistingUserLoginResponseModel loginResponse = api.auth.loginExistingUser(loginData);

        step("Проверка сообщения об ошибке при попытке входа без username", () -> {
            String actualError = loginResponse.username().getFirst();
            assertThat(actualError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorPass);
        });
    }

    @Test
    @Description("POST /auth/token/ с пустым паролем: возвращается ошибка валидации")
    @DisplayName("Ошибка при авторизации без пароля")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void badRequestLoginWhenPasswordMissingTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, "");

        ExistingPasswordLoginResponseModel loginResponse = api.auth.passwordExistingUser(loginData);

        step("Проверка сообщения об ошибке при попытке входа без пароля", () -> {
            String actualError = loginResponse.password().getFirst();
            assertThat(actualError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorPass);
        });
    }
}