package tests;

import io.qameta.allure.*;
import models.clubs.ClubModel;
import models.clubs.ClubsListResponseModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.clubs.ClubsSpec.*;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Управление клубами")
@Feature("Клубы")
@Story("CRUD /api/v1/clubs/")
public class ClubsTests extends TestBase {

    private String nameUser;
    private String accessToken;
    private Integer createdClubId; // для гарантированной очистки

    @BeforeEach
    public void prepareTestData() {
        nameUser = faker.name().username() + "_" + System.currentTimeMillis();
        RegistrationBodyModel registrationData = new RegistrationBodyModel(nameUser, passwordDef);
        api.users.register(registrationData);

        LoginBodyModel loginData = new LoginBodyModel(nameUser, passwordDef);
        accessToken = api.auth.login(loginData).access();
    }

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
        String suffix = String.valueOf(System.currentTimeMillis());
        return new models.clubs.CreateClubBodyModel(
                "QA Guru Club " + suffix,
                "Stanislav Vasenkov",
                2026,
                "Homework CRUD club",
                "https://t.me/qa_guru_" + suffix
        );
    }

    @Test
    @Description("POST /clubs/ с валидными данными: клуб создаётся, возвращается 201, все поля совпадают")
    @DisplayName("Create: успешное создание клуба (201 Created)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void createClubSuccessfully() {
        models.clubs.CreateClubBodyModel clubData = uniqueClubBody();

        ClubModel created = step("Создание клуба", () ->
                api.clubs.createClub(accessToken, clubData)
        );
        createdClubId = created.id();

        step("Проверка созданного клуба", () -> {
            assertThat(created.id()).isPositive();
            assertThat(created.bookTitle()).isEqualTo(clubData.bookTitle());
            assertThat(created.bookAuthors()).isEqualTo(clubData.bookAuthors());
            assertThat(created.publicationYear()).isEqualTo(clubData.publicationYear());
            assertThat(created.description()).isEqualTo(clubData.description());
            assertThat(created.telegramChatLink()).isEqualTo(clubData.telegramChatLink());
            assertThat(created.owner()).isPositive();
            assertThat(created.members()).contains(created.owner());
            assertThat(created.created()).isNotNull();
        });
    }

    @Test
    @Description("GET /clubs/ возвращает список клубов с пагинацией и корректной структурой")
    @DisplayName("Read: получение списка клубов (200 OK)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void getClubsListSuccessfully() {
        ClubsListResponseModel response = api.clubs.getClubs();

        step("Проверить пагинацию и обязательные поля клубов", () -> {
            assertThat(response).isNotNull();
            assertThat(response.count()).isGreaterThanOrEqualTo(0);
            assertThat(response.results()).isNotNull();

            if (!response.results().isEmpty()) {
                for (ClubModel club : response.results()) {
                    assertThat(club.id()).isPositive();
                    assertThat(club.bookTitle()).isNotBlank();
                    assertThat(club.bookAuthors()).isNotBlank();
                    assertThat(club.publicationYear()).isNotNull();
                    assertThat(club.owner()).isPositive();
                    assertThat(club.created()).isNotNull();
                }
            }
        });
    }

    @Test
    @Description("GET /clubs/{id}/ возвращает клуб с корректными данными")
    @DisplayName("Read: получение клуба по id (200 OK)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void getClubByIdSuccessfully() {
        models.clubs.CreateClubBodyModel clubData = uniqueClubBody();
        ClubModel created = api.clubs.createClub(accessToken, clubData);

        ClubModel found = step("Получение клуба по id: " + created.id(), () ->
                api.clubs.getClub(created.id())
        );

        step("Проверка данных клуба", () -> {
            assertThat(found.id()).isEqualTo(created.id());
            assertThat(found.bookTitle()).isEqualTo(created.bookTitle());
            assertThat(found.bookAuthors()).isEqualTo(created.bookAuthors());
            assertThat(found.publicationYear()).isEqualTo(created.publicationYear());
            assertThat(found.description()).isEqualTo(created.description());
            assertThat(found.telegramChatLink()).isEqualTo(created.telegramChatLink());
            assertThat(found.owner()).isEqualTo(created.owner());
        });
    }

    @Test
    @Description("PUT /clubs/{id}/ с новыми данными: клуб обновляется, все поля совпадают")
    @DisplayName("Update: полное обновление клуба через PUT (200 OK)")
    @Tags({
            @Tag("regression"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void updateClubSuccessfully() {
        models.clubs.CreateClubBodyModel createData = uniqueClubBody();
        ClubModel created = api.clubs.createClub(accessToken, createData);
        createdClubId = created.id();

        models.clubs.CreateClubBodyModel updateData = new models.clubs.CreateClubBodyModel(
                createData.bookTitle() + " Updated",
                "Updated Author",
                2025,
                updatedClubDescription,
                createData.telegramChatLink()
        );

        ClubModel updated = step("Обновление клуба через PUT", () ->
                api.clubs.updateClub(accessToken, created.id(), updateData)
        );

        step("Проверка обновлённых данных", () -> {
            assertThat(updated.id()).isEqualTo(created.id());
            assertThat(updated.bookTitle()).isEqualTo(updateData.bookTitle());
            assertThat(updated.bookAuthors()).isEqualTo(updateData.bookAuthors());
            assertThat(updated.publicationYear()).isEqualTo(updateData.publicationYear());
            assertThat(updated.description()).isEqualTo(updateData.description());
            assertThat(updated.telegramChatLink()).isEqualTo(updateData.telegramChatLink());
            assertThat(updated.owner()).isEqualTo(created.owner());
        });
    }

    @Test
    @Description("PATCH /clubs/{id}/ с частичными данными: только указанные поля обновляются")
    @DisplayName("Update: частичное обновление клуба через PATCH (200 OK)")
    @Tags({@Tag("regression"), @Tag("positive")})
    @Severity(SeverityLevel.NORMAL)
    public void patchClubSuccessfully() {
        models.clubs.CreateClubBodyModel createData = uniqueClubBody();
        ClubModel created = api.clubs.createClub(accessToken, createData);
        createdClubId = created.id();

        models.clubs.ClubPatchUpdateDescriptionBodyModel patchData = new models.clubs.ClubPatchUpdateDescriptionBodyModel(
                updatedClubDescription
        );

        ClubModel patched = step("Частичное обновление клуба через PATCH", () ->
                api.clubs.patchClub(accessToken, created.id(), patchData)
        );

        step("Проверка частичного обновления", () -> {
            assertThat(patched.id()).isEqualTo(created.id());
            assertThat(patched.bookTitle()).isEqualTo(created.bookTitle());
            assertThat(patched.bookAuthors()).isEqualTo(created.bookAuthors());
            assertThat(patched.publicationYear()).isEqualTo(created.publicationYear());
            assertThat(patched.description()).isEqualTo(updatedClubDescription);
            assertThat(patched.telegramChatLink()).isEqualTo(created.telegramChatLink());
        });
    }

    @Test
    @Description("DELETE /clubs/{id}/ удаляет клуб, последующий GET возвращает 404")
    @DisplayName("Delete: успешное удаление клуба (204 No Content)")
    @Tags({
            @Tag("regression"),
            @Tag("smoke"),
            @Tag("positive")
    })
    @Severity(SeverityLevel.CRITICAL)
    public void deleteClubSuccessfully() {
        models.clubs.CreateClubBodyModel clubData = uniqueClubBody();
        ClubModel created = api.clubs.createClub(accessToken, clubData);

        step("Удаление клуба с id: " + created.id(), () ->
                api.clubs.deleteClub(accessToken, created.id())
        );

        createdClubId = null;

        step("Проверка, что клуб удалён (404)", () -> {
            var response = api.clubs.getClubWithSpec(created.id(), clubsResponse404Spec);
            assertThat(response.path("detail").toString()).isEqualTo(notFoundError);
        });
    }

    @Test
    @Description("GET /clubs/{id}/ с несуществующим id: возвращается 404")
    @DisplayName("Read: получение несуществующего клуба (404 Not Found)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void getNonExistentClubReturns404() {
        var response = api.clubs.getClubWithSpec(nonExistentClubId, clubsResponse404Spec);

        step("Проверить сообщение об ошибке 404 Not Found", () ->
                assertThat(response.path("detail").toString()).isEqualTo(notFoundError)
        );
    }

    @Test
    @Description("POST /clubs/ без токена: возвращается 401")
    @DisplayName("Create: создание клуба без токена (401 Unauthorized)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void createClubWithoutTokenReturns401() {
        models.clubs.CreateClubBodyModel clubData = uniqueClubBody();

        var response = api.clubs.createClubWithSpec(emptyString, clubData, clubsResponse401Spec);

        step("Проверить сообщение об ошибке авторизации", () ->
                assertThat(response.path("detail").toString()).isEqualTo(unauthorizedError)
        );
    }

    @Test
    @Description("POST /clubs/ с пустым названием: возвращается 400")
    @DisplayName("Create: создание клуба с пустым названием (400 Bad Request)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void createClubWithEmptyTitleReturns400() {
        models.clubs.CreateClubBodyModel valid = uniqueClubBody();
        models.clubs.CreateClubBodyModel clubData = new models.clubs.CreateClubBodyModel(
                emptyString,
                valid.bookAuthors(),
                valid.publicationYear(),
                valid.description(),
                valid.telegramChatLink()
        );

        var response = api.clubs.createClubWithSpec(accessToken, clubData, clubsResponse400Spec);

        step("Проверить валидационную ошибку для поля bookTitle", () ->
                assertThat(response.path("bookTitle[0]").toString()).isEqualTo(expectedErrorPass)
        );
    }

    @Test
    @Description("PUT /clubs/{id}/ несуществующего клуба: возвращается 404")
    @DisplayName("Update: обновление несуществующего клуба (404 Not Found)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void updateNonExistentClubReturns404() {
        models.clubs.CreateClubBodyModel updateData = uniqueClubBody();

        var response = api.clubs.updateClubWithSpec(accessToken, nonExistentClubId, updateData, clubsResponse404Spec);

        step("Проверить сообщение об ошибке 404 Not Found", () ->
                assertThat(response.path("detail").toString()).isEqualTo(notFoundError)
        );
    }

    @Test
    @Description("PATCH /clubs/{id}/ несуществующего клуба: возвращается 404")
    @DisplayName("Update: частичное обновление несуществующего клуба (404 Not Found)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void patchNonExistentClubReturns404() {
        models.clubs.ClubPatchUpdateDescriptionBodyModel patchData = new models.clubs.ClubPatchUpdateDescriptionBodyModel(
                updatedClubDescription
        );

        var response = api.clubs.patchClubWithSpec(accessToken, nonExistentClubId, patchData, clubsResponse404Spec);

        step("Проверить сообщение об ошибке 404 Not Found", () ->
                assertThat(response.path("detail").toString()).isEqualTo(notFoundError)
        );
    }

    @Test
    @Description("DELETE /clubs/{id}/ без токена: возвращается 401")
    @DisplayName("Delete: удаление клуба без токена (401 Unauthorized)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void deleteClubWithoutTokenReturns401() {
        var response = api.clubs.deleteClubWithSpec(emptyString, 1, clubsResponse401Spec);

        step("Проверить сообщение об ошибке авторизации", () ->
                assertThat(response.path("detail").toString()).isEqualTo(unauthorizedError)
        );
    }

    @Test
    @Description("DELETE /clubs/{id}/ несуществующего клуба: возвращается 404")
    @DisplayName("Delete: удаление несуществующего клуба (404 Not Found)")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void deleteNonExistentClubReturns404() {
        var response = api.clubs.deleteClubWithSpec(accessToken, nonExistentClubId, clubsResponse404Spec);

        step("Проверить сообщение об ошибке 404 Not Found", () ->
                assertThat(response.path("detail").toString()).isEqualTo(notFoundError)
        );
    }
}