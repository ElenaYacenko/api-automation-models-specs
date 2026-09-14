package tests;

import io.qameta.allure.*;
import models.registration.*;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.TestData.expectedError;
import static tests.TestData.expectedErrorPass;

@Owner("Elena Yatsenko")
@Epic("Управление пользователями")
@Feature("Регистрация")
public class RegistrationTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    public void prepareTestData() {
        username = "user_" + System.currentTimeMillis();
        password = "pass_" + System.currentTimeMillis();
    }

    @Test
    @Description("POST регистрации с уникальными username/password: id > 0, поля профиля в ожидаемом виде, remoteAddr соответствует regexp.")
    @DisplayName("Успешная регистрация нового пользователя")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void successfulRegistrationTests() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);
        SuccessfulRegistrationResponseModel registrationResponse = api.users.register(registrationData);

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
    @Description("POST /users/register/ с уже существующим username: возвращается ошибка с детализацией")
    @DisplayName("Попытка регистрации с уже существующим username")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void existingUserTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel firstRegistrationResponse = api.users.register(registrationData);

        assertThat(firstRegistrationResponse.username()).isEqualTo(username);

        ExistingUserResponseModel secondRegistrationResponse = api.users.registerExistingUser(registrationData);

        step("Проверка сообщения об ошибке при попытке регистрации существующего пользователя", () -> {
            String actualError = secondRegistrationResponse.username().getFirst();
            assertThat(actualError).isEqualTo(expectedError);
        });
    }

    @Test
    @Description("POST /users/register/ с пустым username: возвращается ошибка валидации")
    @DisplayName("Попытка регистрации без username")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void badRequestWhenUsernameMissingTest() {

        ExistingUserResponseModel response = api.users.registerUsernameMissing(password);

        step("Проверка сообщения об ошибке при попытки регистрации пользователя без username", () -> {
            String actualError = response.username().get(0);
            assertThat(actualError).isEqualTo(expectedErrorPass);
        });
    }

    @Test
    @Description("POST /users/register/ с пустым password: возвращается ошибка валидации")
    @DisplayName("Попытка регистрации без password")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void badRequestWhenPasswordMissingTest() {
        ExistingPasswordResponseModel response = api.users.registerPasswordMissing(username);

        step("Проверка сообщения об ошибке при попытки регистрации пользователя без password", () -> {
            String actualError = response.password().get(0);
            assertThat(actualError).isEqualTo(expectedErrorPass);
        });
    }

    @Test
    @Description("POST /users/register/ с пустыми username и password: возвращаются ошибки валидации")
    @DisplayName("Попытка регистрации пользователя с пустыми username и password")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void badRequestWhenBothFieldsMissingTest() {

        ExistingNoParamResponseModel response = api.users.registerBothMissing();

        step("Проверка сообщений об ошибках при попытке регистрации без username и password", () -> {
            String actualErrorPass = response.password().getFirst();
            String actualErrorName = response.username().getFirst();
            assertThat(actualErrorName).isEqualTo(expectedErrorPass);
            assertThat(actualErrorPass).isEqualTo(expectedErrorPass);
        });
    }
}
