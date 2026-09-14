package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import models.clubs.ClubModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.reviews.*;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.reviews.ReviewsSpec.*;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Отзывы на клубы")
@Feature("Управление отзывами пользователя")
public class ReviewTests extends TestBase {
    private String nameUser;
    private String newUsername;
    private String accessToken;
    private Integer createdClubId; // для гарантированной очистки

    @BeforeEach
    public void prepareTestData() {
        nameUser = faker.name().lastName() + "_" + System.currentTimeMillis();
        newUsername = faker.name().lastName() + "_" + System.currentTimeMillis();

        RegistrationBodyModel registrationData = new RegistrationBodyModel(nameUser, passwordDef);
        api.users.register(registrationData);

        LoginBodyModel loginData = new LoginBodyModel(nameUser, passwordDef);
        accessToken = api.auth.login(loginData).access();

        models.clubs.CreateClubBodyModel clubData = uniqueClubBody();
        ClubModel createdClub = api.clubs.createClub(accessToken, clubData);
        createdClubId = createdClub.id();
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
        return new models.clubs.CreateClubBodyModel(
                "QA Guru, " + faker.book().title()+ "_" + System.currentTimeMillis(),
                faker.book().author(),
                faker.number().numberBetween(2000, 2026),
                faker.lorem().sentence(),
                "https://t.me/" + faker.internet().uuid()
        );
    }

    private ReviewBodyModel uniqueReviewBody() {
        return new ReviewBodyModel(
                createdClubId,
                faker.lorem().sentence(),
                faker.number().numberBetween(1, 6),   // 1..5
                faker.number().numberBetween(1, 500)
        );
    }

    @Test
    @Description("Создание отзыва на созданный клуб")
    @DisplayName("Успешное создание отзыва (201)")
    @Tags({@Tag("regression"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void successfulCreateReviewTest() {
        ReviewBodyModel reviewData = uniqueReviewBody();

        ReviewResponseModel createdReview = step("Создание отзыва через API", () ->
                api.reviews.createReview(accessToken, reviewData)
        );

        step("Проверка полей созданного отзыва", () -> {
            assertThat(createdReview.id())
                    .as("id отзыва должен быть положительным")
                    .isPositive();

            assertThat(createdReview.club())
                    .as("club совпадает с созданным клубом")
                    .isEqualTo(createdClubId);

            assertThat(createdReview.review())
                    .as("текст отзыва совпадает с отправленным")
                    .isEqualTo(reviewData.review());

            assertThat(createdReview.assessment())
                    .as("assessment совпадает с отправленным")
                    .isEqualTo(reviewData.assessment());

            assertThat(createdReview.readPages())
                    .as("readPages совпадает с отправленным")
                    .isEqualTo(reviewData.readPages());

            assertThat(createdReview.created())
                    .as("created не пустой")
                    .isNotNull();

            assertThat(createdReview.user())
                    .as("user не пустой")
                    .isNotNull();

            assertThat(createdReview.user().username())
                    .as("username совпадает с логином")
                    .isEqualTo(nameUser);
        });
    }

    @Test
    @Description("Получение списка отзывов клуба с пагинацией")
    @DisplayName("Успешное получение списка отзывов клуба")
    @Tags({@Tag("regression"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void successfulGetReviewsByClubTest() {

        ReviewsListResponseModel getReview = api.reviews.getReviewsByClub(createdClubId, 1, 1);

        step("Проверить пагинацию и обязательные поля клубов", () -> {
            assertThat(getReview.results())
                    .as("все отзывы должны быть от нашего клуба")
                    .allMatch(r -> r.club().equals(createdClubId));
            assertThat(getReview).isNotNull();
            assertThat(getReview.count()).isGreaterThanOrEqualTo(0);
            assertThat(getReview.results()).isNotNull();
        });
    }

    @Test
    @Description("Получение отзыва по id")
    @DisplayName("Успешное получение отзыва по id")
    @Tags({@Tag("regression"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void successfulGetReviewByIdTest() {
        ReviewBodyModel reviewData = uniqueReviewBody();
        ReviewResponseModel createdReview = api.reviews.createReview(accessToken, reviewData);
        int createdReviewId = createdReview.id();

        ReviewResponseModel fetchedReview = api.reviews.getReview(createdReviewId);

        step("Проверка полей отзыва", () -> {
            assertThat(fetchedReview.id())
                    .as("id совпадает")
                    .isEqualTo(createdReviewId);
            assertThat(fetchedReview.club())
                    .as("club совпадает")
                    .isEqualTo(createdClubId);
            assertThat(fetchedReview.review())
                    .as("текст отзыва совпадает")
                    .isEqualTo(reviewData.review());
            assertThat(fetchedReview.assessment())
                    .as("assessment совпадает")
                    .isEqualTo(reviewData.assessment());
            assertThat(fetchedReview.readPages())
                    .as("readPages совпадает")
                    .isEqualTo(reviewData.readPages());
            assertThat(fetchedReview.user())
                    .as("user не пустой")
                    .isNotNull();
            assertThat(fetchedReview.user().id())
                    .as("user.id положительный")
                    .isPositive();
            assertThat(fetchedReview.user().username())
                    .as("username не пустой")
                    .isNotBlank();
            assertThat(fetchedReview.created())
                    .as("created не пустой")
                    .isNotNull();
        });
    }

    @Test
    @Description("Изменение полностью отзыва")
    @DisplayName("Успешное изменение отзыва по id PUT")
    @Tags({@Tag("regression"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void successfulPutReviewTest() {
        ReviewBodyModel reviewData = uniqueReviewBody();
        ReviewResponseModel createdReview = api.reviews.createReview(accessToken, reviewData);
        int createdReviewId = createdReview.id();

        ReviewBodyModel updateData = new ReviewBodyModel(
                createdClubId,
                faker.lorem().sentence(),
                faker.number().numberBetween(1, 6),
                faker.number().numberBetween(1, 500)
        );

        ReviewResponseModel fetchedReview = api.reviews.updateReviewPut(accessToken, createdReviewId, updateData);

        step("Проверка полей отзыва", () -> {
            assertThat(fetchedReview.id())
                    .as("id совпадает")
                    .isEqualTo(createdReviewId);
            assertThat(fetchedReview.club())
                    .as("club совпадает")
                    .isEqualTo(createdClubId);
            assertThat(fetchedReview.review())
                    .as("текст обновился")
                    .isEqualTo(updateData.review())
                    .isNotEqualTo(reviewData.review());
            assertThat(fetchedReview.assessment())
                    .as("assessment обновился")
                    .isEqualTo(updateData.assessment())
                    .isNotEqualTo(reviewData.assessment());
            assertThat(fetchedReview.readPages())
                    .as("readPages обновился")
                    .isEqualTo(updateData.readPages())
                    .isNotEqualTo(reviewData.readPages());
            assertThat(fetchedReview.user())
                    .as("user не пустой")
                    .isNotNull();
            assertThat(fetchedReview.user().id())
                    .as("user.id положительный")
                    .isPositive();
            assertThat(fetchedReview.user().username())
                    .as("username не пустой")
                    .isNotBlank();
            assertThat(fetchedReview.created())
                    .as("created не пустой")
                    .isNotNull();
        });
    }

    @Test
    @Description("Изменение частичное отзыва")
    @DisplayName("Успешное изменение части отзыва по id PATCH")
    @Tags({@Tag("regression"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void successfulPatchReviewTest() {
        ReviewBodyModel reviewData = uniqueReviewBody();
        ReviewResponseModel createdReview = api.reviews.createReview(accessToken, reviewData);
        int createdReviewId = createdReview.id();

        ReviewPatchBodyModel updateData = new ReviewPatchBodyModel(
                faker.lorem().sentence(),
                faker.number().numberBetween(1, 3),
                faker.number().numberBetween(1, 500)
        );

        ReviewResponseModel fetchedReview = api.reviews.updateReviewPatch(accessToken, createdReviewId, updateData);

        step("Проверка полей отзыва", () -> {
            assertThat(fetchedReview.id())
                    .as("id совпадает")
                    .isEqualTo(createdReviewId);
            assertThat(fetchedReview.club())
                    .as("club совпадает")
                    .isEqualTo(createdClubId);
            assertThat(fetchedReview.review())
                    .as("текст обновился")
                    .isEqualTo(updateData.review())
                    .isNotEqualTo(reviewData.review());
            assertThat(fetchedReview.assessment())
                    .as("assessment обновился")
                    .isEqualTo(updateData.assessment())
                    .isNotEqualTo(reviewData.assessment());
            assertThat(fetchedReview.readPages())
                    .as("readPages обновился")
                    .isEqualTo(updateData.readPages())
                    .isNotEqualTo(reviewData.readPages());
            assertThat(fetchedReview.user())
                    .as("user не пустой")
                    .isNotNull();
            assertThat(fetchedReview.user().id())
                    .as("user.id положительный")
                    .isPositive();
            assertThat(fetchedReview.user().username())
                    .as("username не пустой")
                    .isNotBlank();
            assertThat(fetchedReview.created())
                    .as("created не пустой")
                    .isNotNull();
        });
    }

    @Test
    @Description("Удаление отзыва")
    @DisplayName("Успешное удаление отзыва по id")
    @Tags({@Tag("regression"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void successfulDeleteReviewTest() {
        ReviewBodyModel reviewData = uniqueReviewBody();
        ReviewResponseModel createdReview = api.reviews.createReview(accessToken, reviewData);
        int createdReviewId = createdReview.id();

        api.reviews.deleteReview(accessToken, createdReviewId);

        step("Проверка, что отзыв удалён (404)", () -> {
            var response = api.reviews.getReviewWithSpec(createdReviewId, reviewsResponse404Spec);
            assertThat(response.path("detail").toString()).isEqualTo(errorBookReview);
        });
    }

    @Test
    @Description("Получение отзыва по несуществующему id")
    @DisplayName("Попытка получения не существующего отзыва")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void errorGetReviewByIdTest() {
        int invalidReviewId = 000000;

        Response response = api.reviews.getReviewWithSpec(invalidReviewId, reviewsResponse404Spec);

        step("Валидация ошибки", () -> {
            assertThat(response.path("detail").toString()).isEqualTo(errorBookReview);
        });
    }

    @Test
    @Description("Создание отзыва без оценки (assessment = null) → 400")
    @DisplayName("Негативный: создание отзыва без assessment")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void errorCreateReviewWithoutAssessmentTest() {

        ReviewNotRatingBodyModel body = new ReviewNotRatingBodyModel(
                createdClubId,
                faker.lorem().sentence(),
                faker.number().numberBetween(1, 500)
        );

        Response response = api.reviews.createReviewWithSpec(accessToken, body, reviewsResponse400Spec);

        step("Проверка тела ошибки", () -> {
            assertThat(response.path("assessment[0]").toString())
                    .as("точное сообщение сервера")
                    .isEqualTo(errorAssessment);
        });
    }


    @Test
    @Description("Попытка обновить чужой отзыв через PUT → 403")
    @DisplayName("Негативный: обновление чужого отзыва запрещено")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.NORMAL)
    public void errorUpdateReviewNotOwnerTest() {
        ReviewResponseModel createdReview = api.reviews.createReview(accessToken, uniqueReviewBody());
        Integer reviewId = createdReview.id();
        String originalReviewText = createdReview.review();
        Integer originalAssessment = createdReview.assessment();
        Integer originalReadPages = createdReview.readPages();

        api.users.register(new RegistrationBodyModel(newUsername, passwordDef));
        String secondToken = api.auth.login(new LoginBodyModel(newUsername, passwordDef)).access();

        ReviewBodyModel updateData = new ReviewBodyModel(
                createdClubId,
                "Hacked!",
                1,
                1
        );

        Response response = step("Ошибка 403 при попытки изменить чужой отзыв", () ->
                api.reviews.updateReviewPutWithSpec(secondToken, reviewId, updateData, reviewsResponse403Spec)
        );

        step("Проверка текста ошибки 'You do not have permission to perform this action.'", () -> {
            assertThat(response.path("detail").toString())
                    .as("сообщение о запрете")
                    .isEqualTo(errorPermission);
        });

        step("Проверка, что отзыв не изменился", () -> {
            ReviewResponseModel after = api.reviews.getReview(reviewId);

            assertThat(after.review())
                    .as("текст отзыва не изменился")
                    .isEqualTo(originalReviewText);

            assertThat(after.assessment())
                    .as("assessment не изменился")
                    .isEqualTo(originalAssessment);

            assertThat(after.readPages())
                    .as("readPages не изменился")
                    .isEqualTo(originalReadPages);

            assertThat(after.modified())
                    .as("modified не должен обновиться (отзыв не редактировался)")
                    .isNull();
        });
    }

    @Test
    @Description("Попытка удалить чужой отзыв → 403")
    @DisplayName("Негативный: удаление чужого отзыва запрещено")
    @Tags({@Tag("regression"), @Tag("negative")})
    @Severity(SeverityLevel.CRITICAL)
    public void errorDeleteReviewNotOwnerTest() {
        ReviewResponseModel createdReview = api.reviews.createReview(accessToken, uniqueReviewBody());
        Integer reviewId = createdReview.id();
        String originalReviewText = createdReview.review();
        Integer originalAssessment = createdReview.assessment();
        Integer originalReadPages = createdReview.readPages();

        api.users.register(new RegistrationBodyModel(newUsername, passwordDef));
        String secondToken = api.auth.login(new LoginBodyModel(newUsername, passwordDef)).access();

        Response response = step("Ошибка 403 при попытки удаления чужого отзыва", () ->
                api.reviews.deleteReviewWithSpec(secondToken, reviewId, reviewsResponse403Spec)
        );

        step("Проверка текста ошибки 'You do not have permission to perform this action.'", () -> {
            assertThat(response.path("detail").toString())
                    .as("сообщение о запрете")
                    .isEqualTo(errorPermission);
        });

        step("Проверка, что отзыв не удалился", () -> {
            ReviewResponseModel after = api.reviews.getReview(reviewId);

            assertThat(after.review())
                    .as("Отзыв с текстом есть")
                    .isEqualTo(originalReviewText);

            assertThat(after.assessment())
                    .as("assessment не изменился")
                    .isEqualTo(originalAssessment);

            assertThat(after.readPages())
                    .as("readPages не изменился")
                    .isEqualTo(originalReadPages);
        });
    }
}