package tests;

import io.qameta.allure.*;
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
import static org.assertj.core.api.Assertions.assertThat;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Авторизация и аутентификация")
@Feature("Выход из системы")

public class LogoutTests extends TestBase {

    @Test
    @Description("Логин по API, из ответа берётся refresh-токен; POST logout с этим токеном выполняется без ошибок (завершение сессии на сервере).")
    @DisplayName("Успешный выход из системы (logout)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void successfulLogoutTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);
        String refreshToken = api.auth.loginAndGetRefreshToken(loginData);

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
        api.auth.logout(logoutData);

        step("Проверка успешного выхода из системы", () -> {
            assertThat(logoutData)
                    .as("Ответ на logout не должен быть null")
                    .isNotNull();
        });
    }

    @Test
    @Description("Повторный logout с уже использованным refresh-токеном: возвращается ошибка")
    @DisplayName("Повторный logout с уже использованным токеном")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.NORMAL)
    public void logoutWithAlreadyUsedRefreshTokenTest() {

        LoginBodyModel loginData = new LoginBodyModel(username, password);
        SuccessfulLoginResponseModel loginResponse = api.auth.login(loginData);

        String refreshToken = loginResponse.refresh();
        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
        SuccessfulLogoutResponseModel firstLogoutResponse = api.auth.logout(logoutData);

        assertThat(firstLogoutResponse)
                .as("Ответ на первый logout не должен быть null")
                .isNotNull();

        InvalidTokenLogoutResponseModel secondLogoutResponse = api.auth.logoutBlacklistedToken(logoutData);

        step("Проверка сообщения об ошибке при повторном использовании токена", () -> {
            String actualRefreshError = secondLogoutResponse.detail();
            assertThat(actualRefreshError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorIBlackList);
        });
    }

    @Test
    @Description("POST /auth/logout/ с невалидным токеном: возвращается ошибка")
    @DisplayName("Ошибка при передаче невалидного refresh токена")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void logoutWithInvalidRefreshTokenTest() {

        LogoutBodyModel logoutData = new LogoutBodyModel(invalidToken);
        InvalidTokenLogoutResponseModel logoutResponse = api.auth.logoutInvalidToken(logoutData);

        step("Проверка сообщения об ошибке при невалидном токене", () -> {
            String actualDetailError = logoutResponse.detail();
            assertThat(actualDetailError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorInvalidToken);
        });
    }

    @Test
    @Description("POST /auth/logout/ с пустым токеном: возвращается ошибка валидации")
    @DisplayName("Ошибка при передаче пустого refresh токена")
    @Tags({
            @Tag("regression"),
            @Tag("negative")
    })
    public void logoutWithEmptyRefreshTokenTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel("");
        EmptyTokenLogoutResponseModel logoutResponse = api.auth.logoutEmptyToken(logoutData);

        step("Проверка сообщения об ошибке при попытке logout с пустым токеном", () -> {
            String actualRefreshlError = logoutResponse.refresh().getFirst();
            assertThat(actualRefreshlError)
                    .as("Сообщение об ошибке должно соответствовать ожидаемому")
                    .isEqualTo(expectedErrorPass);
        });
    }
}
