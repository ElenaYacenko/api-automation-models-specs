package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.update.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.login.LoginSpec.successfullLoginResponseSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;
import static specs.update.UpdateSpec.badRequestResponseSpec;
import static specs.update.UpdateSpec.successfulUpdateResponseSpec;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Управление пользователями")
@Feature("Обновление данных пользователя")

public class UpdateUserTests extends TestBase {

    String usernameUser;
    String passwordUser;

    @BeforeEach
    public void prepareTestData() {
        Faker faker = new Faker();
        usernameUser = faker.name().firstName();
        passwordUser = faker.name().firstName();
    }

    @Test
    @DisplayName("Обновление пользователя через PUT (все поля)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    public void successfulUpdatePutTests() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);

        SuccessfulRegistrationResponseModel registrationResponse = step("Регистрация нового пользователя", () ->
                given(baseRequestSpec)
                        .body(registrationData)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(SuccessfulRegistrationResponseModel.class)
        );

        Integer userId = registrationResponse.id();

        assertThat(registrationResponse.username()).isEqualTo(usernameUser);


        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);

        SuccessfulLoginResponseModel loginResponse = step("Авторизация и получение access токена", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract()
                        .as(SuccessfulLoginResponseModel.class)
        );

        String accessToken = loginResponse.access();

        UpdateBodyModel updatePutData = new UpdateBodyModel(newUsername, newFirstName, newLastName, newEmail);

        SuccessfulUpdateResponseModel specificationResponse = step("Обновление всех полей пользователя через PUT", () ->
                given(baseRequestSpec)
                        .auth().oauth2(accessToken)
                        .body(updatePutData)
                        .queryParam("id", userId)
                        .when()
                        .put("/users/me/")
                        .then()
                        .spec(successfulUpdateResponseSpec)
                        .extract()
                        .as(SuccessfulUpdateResponseModel.class)
        );

        step("Проверка обновлённых данных пользователя", () -> {
            assertThat(specificationResponse.id())
                    .as("ID пользователя должен соответствовать ожидаемому")
                    .isEqualTo(userId);

            assertThat(specificationResponse.username())
                    .as("Username должен быть обновлён")
                    .isEqualTo(newUsername);

            assertThat(specificationResponse.firstName())
                    .as("firstName должен быть обновлён")
                    .isEqualTo(newFirstName);

            assertThat(specificationResponse.lastName())
                    .as("lastName должен быть обновлён")
                    .isEqualTo(newLastName);

            assertThat(specificationResponse.email())
                    .as("email должен быть обновлён")
                    .isEqualTo(newEmail);

            assertThat(specificationResponse.remoteAddr())
                    .as("remoteAddr не должен быть null")
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Частичное обновление пользователя через PATCH (только firstName)")
    @Tags({
            @Tag("regression"),
            @Tag("positive")
    })
    public void successfulPatchUserFirstNameOnly() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);

        SuccessfulRegistrationResponseModel registrationResponse = step("Регистрация нового пользователя", () ->
                given(baseRequestSpec)
                        .body(registrationData)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(SuccessfulRegistrationResponseModel.class)
        );

        Integer userId = registrationResponse.id();
        String originalUsername = registrationResponse.username();
        String originalEmail = registrationResponse.email();

        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);

        SuccessfulLoginResponseModel loginResponse = step("Авторизация и получение access токена", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract()
                        .as(SuccessfulLoginResponseModel.class)
        );

        String accessToken = loginResponse.access();

        PatchFirstNameUserBodyModel patchData = new PatchFirstNameUserBodyModel(newFirstName);

        SuccessfulUpdateResponseModel specificationResponse = step("Обновление только firstName через PATCH", () ->
                given(baseRequestSpec)
                        .auth().oauth2(accessToken)
                        .body(patchData)
                        .queryParam("id", userId)
                        .when()
                        .patch("/users/me/")
                        .then()
                        .spec(successfulUpdateResponseSpec)
                        .extract()
                        .as(SuccessfulUpdateResponseModel.class)
        );

        step("Проверка частичного обновления (только firstName)", () -> {
            assertThat(specificationResponse.id())
                    .as("ID пользователя должен соответствовать ожидаемому")
                    .isEqualTo(userId);

            assertThat(specificationResponse.username())
                    .as("Username не должен измениться")
                    .isEqualTo(originalUsername);

            assertThat(specificationResponse.firstName())
                    .as("firstName должен быть обновлён")
                    .isEqualTo(newFirstName);

            assertThat(specificationResponse.lastName())
                    .as("lastName должен остаться пустым")
                    .isEqualTo("");

            assertThat(specificationResponse.email())
                    .as("email не должен измениться")
                    .isEqualTo(originalEmail);

            assertThat(specificationResponse.remoteAddr())
                    .as("remoteAddr не должен быть null")
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Частичное обновление пользователя через PATCH (только email)")
    @Tags({
            @Tag("regression"),
            @Tag("positive")
    })
    public void successfulPatchUserEmailOnly() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);

        SuccessfulRegistrationResponseModel registrationResponse = step("Регистрация нового пользователя", () ->
                given(baseRequestSpec)
                        .body(registrationData)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(SuccessfulRegistrationResponseModel.class)
        );

        Integer userId = registrationResponse.id();
        String originalUsername = registrationResponse.username();

        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);

        SuccessfulLoginResponseModel loginResponse = step("Авторизация и получение access токена", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract()
                        .as(SuccessfulLoginResponseModel.class)
        );

        String accessToken = loginResponse.access();

        PatchEmailUserBodyModel patchData = new PatchEmailUserBodyModel(newEmail);

        SuccessfulUpdateResponseModel specificationResponse = step("Обновление только email через PATCH", () ->
                given(baseRequestSpec)
                        .auth().oauth2(accessToken)
                        .body(patchData)
                        .queryParam("id", userId)
                        .when()
                        .patch("/users/me/")
                        .then()
                        .spec(successfulUpdateResponseSpec)
                        .extract()
                        .as(SuccessfulUpdateResponseModel.class)
        );

        step("Проверка частичного обновления (только email)", () -> {
            assertThat(specificationResponse.id())
                    .as("ID пользователя должен соответствовать ожидаемому")
                    .isEqualTo(userId);

            assertThat(specificationResponse.username())
                    .as("Username не должен измениться")
                    .isEqualTo(originalUsername);

            assertThat(specificationResponse.firstName())
                    .as("firstName должен остаться пустым")
                    .isEqualTo("");

            assertThat(specificationResponse.lastName())
                    .as("lastName должен остаться пустым")
                    .isEqualTo("");

            assertThat(specificationResponse.email())
                    .as("email должен быть обновлён")
                    .isEqualTo(newEmail);

            assertThat(specificationResponse.remoteAddr())
                    .as("remoteAddr не должен быть null")
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Обновление пользователя через PUT с пустым username (негативный)")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void updateUserWithEmptyUsernameTest() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);

        SuccessfulRegistrationResponseModel registrationResponse = step("Регистрация нового пользователя", () ->
                given(baseRequestSpec)
                        .body(registrationData)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(SuccessfulRegistrationResponseModel.class)
        );

        Integer userId = registrationResponse.id();


        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);

        SuccessfulLoginResponseModel loginResponse = step("Авторизация и получение access токена", () ->
                given(baseRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfullLoginResponseSpec)
                        .extract()
                        .as(SuccessfulLoginResponseModel.class)
        );

        String accessToken = loginResponse.access();

        PatchUsernameUserBodyModel updatePutData = new PatchUsernameUserBodyModel("");

        PatchExistingErrorResponseModel errorResponse = step(
                "Обновление пользователя с пустым username (ожидаем ошибку)", () ->
                        given(baseRequestSpec)
                                .auth().oauth2(accessToken)
                                .body(updatePutData)
                                .queryParam("id", userId)
                                .when()
                                .put("/users/me/")
                                .then()
                                .spec(badRequestResponseSpec)
                                .extract()
                                .as(PatchExistingErrorResponseModel.class)
        );

        assertThat(errorResponse.username().get(0)).isEqualTo("This field may not be blank.");
        assertThat(errorResponse.firstName().get(0)).isEqualTo("This field is required.");
        assertThat(errorResponse.lastName().get(0)).isEqualTo("This field is required.");
        assertThat(errorResponse.email().get(0)).isEqualTo("This field is required.");
    }
}