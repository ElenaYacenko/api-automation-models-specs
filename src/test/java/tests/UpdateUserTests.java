package tests;

import io.qameta.allure.*;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.update.PatchExistingErrorResponseModel;
import models.update.SuccessfulUpdateResponseModel;
import models.update.UpdateBodyModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Управление пользователями")
@Feature("Обновление данных пользователя")

public class UpdateUserTests extends TestBase {

    String usernameUser;
    String passwordUser;

    @BeforeEach
    public void prepareTestData() {

        usernameUser = "user_" + System.currentTimeMillis();
        passwordUser = "pass_" + System.currentTimeMillis();
    }

    @Test
    @Description("Регистрация нового пользователя, логин, получение access-токена, PUT /users/me/ с полным набором полей: профиль обновляется корректно.")
    @DisplayName("Обновление пользователя через PUT (все поля)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void successfulUpdatePutTests() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);
        SuccessfulRegistrationResponseModel registrationResponse = api.users.register(registrationData);
        Integer userId = registrationResponse.id();
        assertThat(registrationResponse.username()).isEqualTo(usernameUser);

        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);
        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);
        String accessToken = loginResponse.access();

        UpdateBodyModel updatePutData = new UpdateBodyModel(newUsername, newFirstName, newLastName, newEmail);
        SuccessfulUpdateResponseModel specificationResponse = api.users.updateUserPut(accessToken, userId, updatePutData);

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
    @Description("Регистрация, логин, PATCH /users/me/ только с firstName: другие поля не изменяются.")
    @DisplayName("Частичное обновление пользователя через PATCH (только firstName)")
    @Tags({
            @Tag("regression"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.NORMAL)
    public void successfulPatchUserFirstNameOnly() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);
        SuccessfulRegistrationResponseModel registrationResponse = api.users.register(registrationData);

        Integer userId = registrationResponse.id();
        String originalUsername = registrationResponse.username();
        String originalEmail = registrationResponse.email();

        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);
        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);
        String accessToken = loginResponse.access();

        SuccessfulUpdateResponseModel specificationResponse = api.users.patchUserFirstName(accessToken, userId, newFirstName);

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
    @Description("Регистрация, логин, PATCH /users/me/ только с email: другие поля не изменяются.")
    @DisplayName("Частичное обновление пользователя через PATCH (только email)")
    @Tags({
            @Tag("regression"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.NORMAL)
    public void successfulPatchUserEmailOnly() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);
        SuccessfulRegistrationResponseModel registrationResponse = api.users.register(registrationData);

        Integer userId = registrationResponse.id();
        String originalUsername = registrationResponse.username();

        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);
        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);

        String accessToken = loginResponse.access();

        SuccessfulUpdateResponseModel specificationResponse = api.users.patchUserEmail(accessToken, userId, newEmail);

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
    @Description("PUT /users/me/ с пустым username: возвращаются ошибки валидации по всем обязательным полям.")
    @DisplayName("Обновление пользователя через PUT с пустым username (негативный)")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    @Severity(SeverityLevel.NORMAL)
    public void updateUserWithEmptyUsernameTest() {

        RegistrationBodyModel registrationData = new RegistrationBodyModel(usernameUser, passwordUser);
        SuccessfulRegistrationResponseModel registrationResponse = api.users.register(registrationData);
        Integer userId = registrationResponse.id();


        LoginBodyModel loginData = new LoginBodyModel(usernameUser, passwordUser);

        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);
        String accessToken = loginResponse.access();

        PatchExistingErrorResponseModel errorResponse = api.users.updateUserWithEmptyUsername(accessToken, userId);

        assertThat(errorResponse.username().get(0)).isEqualTo("This field may not be blank.");
        assertThat(errorResponse.firstName().get(0)).isEqualTo("This field is required.");
        assertThat(errorResponse.lastName().get(0)).isEqualTo("This field is required.");
        assertThat(errorResponse.email().get(0)).isEqualTo("This field is required.");
    }
}