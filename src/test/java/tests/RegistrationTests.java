package tests;

import models.registration.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
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
    @DisplayName("Успешная регистрация нового пользователя")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    public void successfulRegistrationTests() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);
        SuccessfulRegistrationResponseModel registrationResponse = step(
                "Регистрация нового пользователя по логину и паролю", () ->
                        given(baseRequestSpec)
                                .body(registrationData)
                                .when()
                                .post("/users/register/")
                                .then()
                                .spec(successfulRegistrationResponseSpec)
                                .extract()
                                .as(SuccessfulRegistrationResponseModel.class)
        );

        step("Валидация ответа регистрации", () -> {
            assertThat(registrationResponse.username())
                    .as("Username должен соответствовать переданному")
                    .isEqualTo(username);

            assertThat(registrationResponse.id())
                    .as("ID должен быть целым числом")
                    .isInstanceOf(Integer.class);

            assertThat(registrationResponse.firstName())
                    .as("firstName должен быть пустой строкой")
                    .isEqualTo("");

            assertThat(registrationResponse.lastName())
                    .as("lastName должен быть пустой строкой")
                    .isEqualTo("");

            assertThat(registrationResponse.email())
                    .as("email должен быть пустой строкой")
                    .isEqualTo("");
        });
    }

    @Test
    @DisplayName("Попытка регистрации с уже существующим username")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void existingUserTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

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

        assertThat(registrationResponse.username()).isEqualTo(username);

        ExistingUserResponseModel response = step("Повторная регистрация с тем же username (ожидаем ошибку)", () ->
                given(baseRequestSpec)
                        .body(registrationData)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(existingUserResponseSpec)
                        .extract()
                        .as(ExistingUserResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытке регистрации существующего пользователя", () -> {
            String actualError = response.username().getFirst();
            assertThat(actualError).isEqualTo(expectedError);
        });
    }

    @Test
    @DisplayName("Попытка регистрации без username")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void badRequestWhenUsernameMissingTest() {

        ExistingUserResponseModel response = step(
                "Регистрация нового пользователя без username", () ->
                        given(baseRequestSpec)
                                .body(new RegistrationBodyModel("", password))
                                .when()
                                .post("/users/register/")
                                .then()
                                .spec(existingUserResponseSpec)
                                .extract()
                                .as(ExistingUserResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытки регистрации пользователя без username", () -> {
            String actualError = response.username().getFirst();
            assertThat(actualError).isEqualTo(expectedErrorPass);
        });
    }

    @Test
    @DisplayName("Попытка регистрации без password")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void badRequestWhenPasswordMissingTest() {

        ExistingPasswordResponseModel response = step(
                "Регистрация нового пользователя без password", () ->
                        given(baseRequestSpec)
                                .body(new RegistrationBodyModel(username, ""))
                                .when()
                                .post("/users/register/")
                                .then()
                                .spec(existingPasswordResponseSpec)
                                .extract()
                                .as(ExistingPasswordResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытки регистрации пользователя без password", () -> {
            String actualError = response.password().getFirst();
            assertThat(actualError).isEqualTo(expectedErrorPass);
        });
    }

    @Test
    @DisplayName("Попытка регистрации пользователя с пустыми username и password")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void badRequestWhenBothFieldsMissingTest() {

        ExistingNoParamResponseModel response = step(
                "Регистрация нового пользователя без username и password", () ->
                        given(baseRequestSpec)
                                .body(new RegistrationBodyModel("", ""))
                                .when()
                                .post("/users/register/")
                                .then()
                                .spec(existingMissingResponseSpec)
                                .extract()
                                .as(ExistingNoParamResponseModel.class)
        );

        step("Проверка сообщения об ошибке при попытки регистрации пользователя без password", () -> {
            String actualErrorPass = response.password().getFirst();
            String actualErrorName = response.username().getFirst();
            assertThat(actualErrorName).isEqualTo(expectedErrorPass);
            assertThat(actualErrorPass).isEqualTo(expectedErrorPass);
        });
    }
}
