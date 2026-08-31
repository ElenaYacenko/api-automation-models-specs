package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.logout.EmptyTokenLogoutResponseModel;
import models.logout.InvalidTokenLogoutResponseModel;
import models.logout.LogoutBodyModel;
import models.logout.SuccessfulLogoutResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.login.LoginSpec.successfullLoginResponseSpec;
import static specs.logout.LogoutSpec.*;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Авторизация и аутентификация")
@Feature("Выход из системы")

public class LogoutTests extends TestBase {

    @Test
    @DisplayName("Успешный выход из системы (logout)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    public void successfulLogoutTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        String refreshToken = step("Авторизация и получение refresh токена", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract().path("refresh"));

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
        SuccessfulLogoutResponseModel logoutResponse = step("Отправка запроса на выход из системы", () ->
                given(baseRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(successfullLogoutResponseSpec)
                        .extract()
                        .as(SuccessfulLogoutResponseModel.class)
        );

        step("Проверка успешного выхода из системы", () -> {
            assertThat(logoutResponse)
                    .as("Ответ на logout не должен быть null")
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Повторный logout с уже использованным токеном")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    public void logoutWithAlreadyUsedRefreshTokenTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        SuccessfulLoginResponseModel loginResponse = step("Авторизация и получение токенов", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract().as(SuccessfulLoginResponseModel.class)
        );

        String refreshToken = loginResponse.refresh();

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);

        SuccessfulLogoutResponseModel firstLogoutResponse = step("Первый выход из системы (успешный)", () ->
                given(baseRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(successfullLogoutResponseSpec)
                        .extract()
                        .as(SuccessfulLogoutResponseModel.class)
        );

        assertThat(firstLogoutResponse)
                .as("Ответ на первый logout не должен быть null")
                .isNotNull();

        InvalidTokenLogoutResponseModel secondLogoutResponse = step(
                "Повторный выход с тем же токеном (ожидаем ошибку)", () ->
                        given(baseRequestSpec)
                                .body(logoutData)
                                .when()
                                .post("/auth/logout/")
                                .then()
                                .spec(blackListRefreshToken)
                                .extract()
                                .as(InvalidTokenLogoutResponseModel.class)
        );

        step("Проверка сообщения об ошибке при повторном использовании токена", () -> {
            String actualRefreshError = secondLogoutResponse.detail();
            assertThat(actualRefreshError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorIBlackList);
        });
    }

    @Test
    @DisplayName("Ошибка при передаче невалидного refresh токена")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void logoutWithInvalidRefreshTokenTest() {

        LogoutBodyModel logoutData = new LogoutBodyModel(invalidToken);
        InvalidTokenLogoutResponseModel logoutResponse = step("Отправка запроса logout с невалидным токеном", () ->
                given(baseRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(invalidRefreshToken)
                        .extract().as(InvalidTokenLogoutResponseModel.class)
        );

        step("Проверка сообщения об ошибке при невалидном токене", () -> {
            String actualDetailError = logoutResponse.detail();
            assertThat(actualDetailError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorInvalidToken);
        });
    }

    @Test
    @DisplayName("Ошибка при передаче пустого refresh токена")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void logoutWithEmptyRefreshTokenTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel("");
        EmptyTokenLogoutResponseModel logoutResponse = step("Отправка запроса logout с пустым токеном", () ->
                given(baseRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(emptyRefreshToken)
                        .extract().as(EmptyTokenLogoutResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытке logout с пустым токеном", () -> {
            String actualRefreshlError = logoutResponse.refresh().getFirst();
            assertThat(actualRefreshlError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorPass);
        });
    }
}
