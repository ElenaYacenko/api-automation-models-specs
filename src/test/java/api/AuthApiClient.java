package api;

import io.qameta.allure.Step;
import models.login.*;
import models.logout.LogoutBodyModel;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.baseRequestSpec;
import static specs.login.LoginSpec.*;
import static specs.logout.LogoutSpec.*;

public class AuthApiClient {

    @Step("[API] Авторизация POST /auth/token/")
    public SuccessfulLoginResponseModel login(LoginBodyModel loginBody) {
        return given(baseRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfullLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);
    }

    @Step("[API] Авторизация и получение токена")
    public String loginAndGetRefreshToken(LoginBodyModel loginBody) {
        return given(baseRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfullLoginResponseSpec)
                .extract()
                .path("refresh");
    }

    @Step("[API] Попытка авторизации с неверными учётными данными POST /auth/token/")
    public WrongCredentialsLoginResponseModel loginWrongCredentials(LoginBodyModel loginBody) {
        return given(baseRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialsLoginResponseSpec)
                .extract()
                .as(WrongCredentialsLoginResponseModel.class);
    }

    @Step("[API] Попытка авторизации без username POST /auth/token/")
    public ExistingUserLoginResponseModel loginExistingUser(LoginBodyModel loginBody) {
        return given(baseRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(existingUserLoginResponseSpec)
                .extract()
                .as(ExistingUserLoginResponseModel.class);
    }

    @Step("[API] Попытка авторизации без password POST /auth/token/")
    public ExistingPasswordLoginResponseModel passwordExistingUser(LoginBodyModel loginBody) {
        return given(baseRequestSpec)
                .body(loginBody)
                .when()
                .post("/auth/token/")
                .then()
                .spec(existingPasswordLoginResponseSpec)
                .extract()
                .as(ExistingPasswordLoginResponseModel.class);
    }

    @Step("[API] Отправка запроса logout")
    public void logout(LogoutBodyModel logoutBody) {
        given(baseRequestSpec)
                .body(logoutBody)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfullLogoutResponseSpec);
    }
}