package tests;

import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.logout.EmptyTokenLogoutResponseModel;
import models.logout.InvalidTokenLogoutResponseModel;
import models.logout.LogoutBodyModel;
import models.logout.SuccessfulLogoutResponseModel;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.Login.LoginSpec.successfullLoginResponseSpec;
import static specs.Logout.LogoutSpec.*;
import static tests.TestData.*;

public class LogoutTests extends TestBase {

    @Test
    public void successfulLogoutTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        String refreshToken = step("Авторизация и получение токена", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract().path("refresh"));
        
        step("Отправка запроса logout и проверка ответа (200)", () -> {
            LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
            SuccessfulLogoutResponseModel logoutResponse = given(baseRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(successfullLogoutResponseSpec)
                    .extract()
                    .as(SuccessfulLogoutResponseModel.class);

            assertThat(logoutResponse).isNotNull();
        });
        }

        @Test
        public void logoutWithInvalidRefreshTokenTest () {

            LogoutBodyModel logoutData = new LogoutBodyModel(invalidToken);
            InvalidTokenLogoutResponseModel logoutResponse = given(baseRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(invalidRefreshToken)
                    .extract().as(InvalidTokenLogoutResponseModel.class);

            String actualDetailError = logoutResponse.detail();
            assertThat(actualDetailError).isEqualTo(expectedErrorInvalidToken);
        }

    @Test
    public void logoutWithEmptyRefreshTokenTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel("");
        EmptyTokenLogoutResponseModel logoutResponse = given(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(emptyRefreshToken)
                .extract().as(EmptyTokenLogoutResponseModel.class);

        String actualRefreshlError = logoutResponse.refresh().getFirst();
        assertThat(actualRefreshlError).isEqualTo(expectedErrorPass);
    }

    @Test
    public void logoutWithAlreadyUsedRefreshTokenTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        SuccessfulLoginResponseModel loginResponse = given(baseRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfullLoginResponseSpec)
                .extract().as(SuccessfulLoginResponseModel.class);

        String refreshToken = loginResponse.refresh();

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);

        SuccessfulLogoutResponseModel firstLogoutResponse = given(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfullLogoutResponseSpec)
                .extract()
                .as(SuccessfulLogoutResponseModel.class);

        assertThat(firstLogoutResponse).isNotNull();

        InvalidTokenLogoutResponseModel secondLogoutResponse = given(baseRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(blackListRefreshToken)
                .extract()
                .as(InvalidTokenLogoutResponseModel.class);

        String actualRefreshError = secondLogoutResponse.detail();
        assertThat(actualRefreshError).isEqualTo(expectedErrorIBlackList);
    }
}
