package api;

import io.qameta.allure.Step;
import models.registration.*;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.baseRequestSpec;
import static specs.registration.RegistrationSpec.*;

public class UsersApiClient {

    @Step("[API] Регистрация пользователя POST /users/register/")
    public SuccessfulRegistrationResponseModel register(RegistrationBodyModel body) {
        return given(baseRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);
    }

    @Step("[API] Попытка повторной регистрации существующего пользователя POST /users/register/")
    public ExistingUserResponseModel registerExistingUser(RegistrationBodyModel body) {
        return given(baseRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(existingUserResponseSpec)
                .extract()
                .as(ExistingUserResponseModel.class);
    }

    @Step("[API] Попытка регистрации пользователя без username POST /users/register/")
    public ExistingUserResponseModel registerUsernameMissing(String password) {
        RegistrationBodyModel body = new RegistrationBodyModel("", password);
        return given(baseRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(existingUserResponseSpec)
                .extract()
                .as(ExistingUserResponseModel.class);
    }

    @Step("[API] Попытка регистрации пользователя без password POST /users/register/")
    public ExistingPasswordResponseModel registerPasswordMissing(String username) {
        RegistrationBodyModel body = new RegistrationBodyModel(username, "");
        return given(baseRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(existingPasswordResponseSpec)
                .extract()
                .as(ExistingPasswordResponseModel.class);
    }

    @Step("[API] Попытка регистрации пользователя без username и password POST /users/register/")
    public ExistingNoParamResponseModel registerBothMissing() {
        RegistrationBodyModel body = new RegistrationBodyModel("", "");
        return given(baseRequestSpec)
                .body(body)
                .when()
                .post("/users/register/")
                .then()
                .spec(existingMissingResponseSpec)
                .extract()
                .as(ExistingNoParamResponseModel.class);
    }
}