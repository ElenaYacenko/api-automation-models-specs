package tests;

import io.qameta.allure.*;
import models.login.SuccessfulLoginResponseModel;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Owner("Elena Yatsenko")
@Epic("Клубы")
@Feature("Управление клубами пользователя")
@Story("Создание, просмотр и удаление клубов")
public class ClubsUITests extends TestBase {
    String username;
    String password;
    String accessToken;
    Integer createdClubId;

    @AfterEach
    public void cleanup() {
        if (createdClubId != null) {
            step("Очистка: удаление созданного клуба с id " + createdClubId, () ->
                    api.clubs.deleteClub(accessToken, createdClubId)
            );
            createdClubId = null;
        }
    }

    private models.clubs.CreateClubBodyModel uniqueClubBody() {
        return new models.clubs.CreateClubBodyModel(
                "QA Guru, " + faker.book().title() + "_" + System.currentTimeMillis(),
                faker.book().author(),
                faker.number().numberBetween(2000, 2026),
                faker.lorem().sentence(),
                "https://t.me/" + faker.internet().uuid()
        );
    }

    @Test
    @Description("Пользователь регистрируется и создает клуб через API. Затем переходит на страницу клуба через UI " +
            "и проверяет, что все данные (название, автор, описание) отображаются корректно.")
    @DisplayName("[Гибридный] Созданный через API клуб корректно отображается на странице (UI)")
    @Tags({@Tag("smoke"), @Tag("api"), @Tag("ui")})
    @Severity(SeverityLevel.CRITICAL)
    public void clubIsVisibleOnPage() {
        SuccessfulLoginResponseModel login = step("Регистрация и логин через API",
                () -> clubPage.openBlankPageWithNewUser()
        );
        accessToken = login.access();

        models.clubs.ClubModel club = step("Создание клуба через API",
                () -> api.clubs.createClub(accessToken, uniqueClubBody())
        );

        String clubId = club.id().toString();
        String contentClub = club.description();
        String authorsClub = club.bookAuthors();
        String titleClub = club.bookTitle();

        step("UI: открыть страницу клуба и проверить что клуб создан с правильными данными", () ->
                clubPage.openPageById(clubId)
                        .verifyClub(titleClub, authorsClub, contentClub)
        );
    }

    @Test
    @Description("Проверка бизнес-правила: при попытке владельца клуба нажать кнопку 'Покинуть клуб', система должна " +
            "показать ошибку 'Не удалось покинуть клуб'.")
    @DisplayName("[UI] Владелец клуба не может покинуть свой клуб")
    @Tags({@Tag("ui"), @Tag("smoke"), @Tag("api")})
    @Severity(SeverityLevel.CRITICAL)
    public void cantLeaveClubAsOwnerTest() {
        SuccessfulLoginResponseModel loginResponse = clubPage.openBlankPageWithNewUser();
        String accessToken = loginResponse.access();

        models.clubs.ClubModel createdClub = api.clubs.createClub(accessToken, uniqueClubBody());
        String clubId = createdClub.id().toString();

        step("UI: Попытка покинуть клуб и проверка появления ошибки для владельца", () ->
                clubPage.openPageById(clubId)
                        .pressLeaveClubButton()
                        .assertOwnerCannotLeaveClub()
        );
    }

    @Test
    @Description("Пользователь заполняет форму создания клуба на UI. Затем через API выполняется поиск клуба " +
            "по названию для проверки, что он действительно создался в БД с корректными данными.")
    @DisplayName("[UI] Создание клуба через интерфейс с последующей проверкой через API")
    @Tags({@Tag("ui"), @Tag("smoke"), @Tag("api")})
    @Severity(SeverityLevel.CRITICAL)
    public void createClubFromUi() {
        SuccessfulLoginResponseModel login = step("Регистрация и логин через API",
                () -> clubPage.openBlankPageWithNewUser()
        );
        accessToken = login.access();

        models.clubs.CreateClubBodyModel clubData = uniqueClubBody();

        step("UI: Переход на главную, открытие формы и создание клуба", () ->
                clubPage
                        .openMainPage()
                        .createdClubButton()
                        .createMyClub(
                                clubData.bookTitle(),
                                clubData.bookAuthors(),
                                clubData.publicationYear().toString(),
                                clubData.description(),
                                clubData.telegramChatLink()
                        )
        );

        step("API: Проверка сохранения клуба в базе данных", () -> {
            models.clubs.ClubsListResponseModel clubs = api.clubs.searchClubs(clubData.bookTitle());

            assertThat(clubs.count())
                    .as("должен найтись ровно 1 клуб с таким title")
                    .isEqualTo(1);

            models.clubs.ClubModel created = clubs.results().get(0);
            createdClubId = created.id();
            assertThat(created.bookTitle()).isEqualTo(clubData.bookTitle());
            assertThat(created.bookAuthors()).isEqualTo(clubData.bookAuthors());
            assertThat(created.description()).isEqualTo(clubData.description());
        });
    }
}