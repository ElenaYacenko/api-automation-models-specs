package api;

import io.qameta.allure.Step;
import models.registration.*;
import models.update.*;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.baseRequestSpec;
import static specs.registration.RegistrationSpec.*;
import static specs.update.UpdateSpec.badRequestResponseSpec;
import static specs.update.UpdateSpec.successfulUpdateResponseSpec;

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

    @Step("[API] Обновление всех полей пользователя через PUT")
    public SuccessfulUpdateResponseModel updateUserPut(String accessToken, Integer userId, UpdateBodyModel body) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .queryParam("id", userId)
                .when()
                .put("/users/me/")
                .then()
                .spec(successfulUpdateResponseSpec)
                .extract()
                .as(SuccessfulUpdateResponseModel.class);
    }

    @Step("[API] Частичное обновление firstName через PATCH")
    public SuccessfulUpdateResponseModel patchUserFirstName(String accessToken, Integer userId, String firstName) {
        PatchFirstNameUserBodyModel body = new PatchFirstNameUserBodyModel(firstName);
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .queryParam("id", userId)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successfulUpdateResponseSpec)
                .extract()
                .as(SuccessfulUpdateResponseModel.class);
    }

    @Step("[API] Частичное обновление email через PATCH")
    public SuccessfulUpdateResponseModel patchUserEmail(String accessToken, Integer userId, String email) {
        PatchEmailUserBodyModel body = new PatchEmailUserBodyModel(email);
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .queryParam("id", userId)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successfulUpdateResponseSpec)
                .extract()
                .as(SuccessfulUpdateResponseModel.class);
    }

    @Step("[API] Попытка обновления пользователя с пустым username (негативный)")
    public PatchExistingErrorResponseModel updateUserWithEmptyUsername(String accessToken, Integer userId) {
        PatchUsernameUserBodyModel body = new PatchUsernameUserBodyModel("");
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .queryParam("id", userId)
                .when()
                .put("/users/me/")
                .then()
                .spec(badRequestResponseSpec)
                .extract()
                .as(PatchExistingErrorResponseModel.class);
    }
}