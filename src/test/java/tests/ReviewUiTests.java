package tests;

import io.qameta.allure.*;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.reviews.ReviewBodyModel;
import models.reviews.ReviewResponseModel;
import models.reviews.ReviewsListResponseModel;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.reviews.ReviewsSpec.reviewsResponse404Spec;
import static tests.TestData.*;

@Owner("Elena Yatsenko")
@Epic("Отзывы на клубы")
@Feature("Управление отзывами пользователя")
@Story("Создание, просмотр и удаление отзывов")
public class ReviewUiTests extends TestBase {
    private String accessToken;
    private Integer createdClubId;
    private String newUsername;

    @BeforeEach
    public void prepareTestData() {
        newUsername = faker.name().lastName() + "_" + System.currentTimeMillis();
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
                "QA Guru, " + faker.book().title() + "_" + System.currentTimeMillis(),
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
    @Description("Проверка того, что отзыв, созданный через API-метод, успешно рендерится на UI странице клуба " +
            "с корректным текстом и автором.")
    @DisplayName("[Гибридный] Отзыв, созданный через API, корректно отображается на странице клуба (UI)")
    @Tags({@Tag("ui"), @Tag("api"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void reviewIsVisibleOnClubPage() {
        SuccessfulLoginResponseModel login = step("Регистрация и логин через API",
                () -> clubPage.openBlankPageWithNewUser()
        );
        accessToken = login.access();

        models.clubs.ClubModel club = step("Создание клуба через API",
                () -> api.clubs.createClub(accessToken, uniqueClubBody())
        );
        createdClubId = club.id();

        ReviewResponseModel createdReview = step("Создание отзыва через API",
                () -> api.reviews.createReview(accessToken, uniqueReviewBody())
        );
        String content = createdReview.review();
        String author = createdReview.user().username();

        step("UI: Проверка отображения отзыва на странице клуба", () -> {
            clubPage.openPageById(String.valueOf(createdClubId));
            reviewPage.verifyReview(author, content);
        });
    }

    @Test
    @Description("Пользователь открывает страницу своего клуба, заполняет форму отзыва и отправляет её. " +
            "Проверяется отображение на UI и сохранение в БД через API.")
    @DisplayName("[UI] Создание отзыва через форму на странице клуба")
    @Tags({@Tag("ui"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void createReviewFromUi() {
        SuccessfulLoginResponseModel login = step("Регистрация и логин через API",
                () -> clubPage.openBlankPageWithNewUser()
        );
        accessToken = login.access();

        models.clubs.ClubModel club = step("Создание клуба через API",
                () -> api.clubs.createClub(accessToken, uniqueClubBody())
        );
        createdClubId = club.id();
        String contentClub = club.description();
        String authorsClub = club.bookAuthors();
        String titleClub = club.bookTitle();
        ReviewBodyModel reviewData = uniqueReviewBody();
        String reviewText = reviewData.review();
        int assessment = reviewData.assessment();
        int readPages = reviewData.readPages();

        step("UI: открыть страницу клуба и форму отзыва", () ->
                clubPage.openPageById(String.valueOf(createdClubId))
                        .verifyClub(titleClub, authorsClub, contentClub)
                        .pressAddReviewButton()
        );

        step("UI: заполнить форму и отправить отзыв", () ->
                reviewPage.fillReviewForm(assessment, readPages, reviewText)
                        .submitReview()
        );

        step("API: проверка, что отзыв создан с правильными данными", () -> {
            ReviewsListResponseModel reviews = api.reviews.getReviewsByClub(createdClubId, 1, 100);

            assertThat(reviews.count())
                    .as("должен быть ровно 1 отзыв")
                    .isEqualTo(1);

            ReviewResponseModel created = reviews.results().get(0);
            assertThat(created.review()).isEqualTo(reviewText);
            assertThat(created.assessment()).isEqualTo(assessment);
            assertThat(created.readPages()).isEqualTo(readPages);
            assertThat(created.club()).isEqualTo(createdClubId);
        });
    }

    @Test
    @Description("Пользователь создает отзыв, затем удаляет его через UI с подтверждением в модальном окне. " +
            "Проверяется исчезновение на UI и возврат 404 по API.")
    @DisplayName("[UI] Удаление своего отзыва через интерфейс клуба")
    @Tags({@Tag("ui"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void deleteReviewFromUi() {
        SuccessfulLoginResponseModel login = step("Регистрация и логин через API",
                () -> clubPage.openBlankPageWithNewUser()
        );
        accessToken = login.access();

        models.clubs.ClubModel club = step("Создание клуба через API",
                () -> api.clubs.createClub(accessToken, uniqueClubBody())
        );
        createdClubId = club.id();

        ReviewResponseModel createdReview = step("Создание отзыва через API",
                () -> api.reviews.createReview(accessToken, uniqueReviewBody())
        );
        int createdReviewId = createdReview.id();
        String content = createdReview.review();

        step("UI: Открытие страницы клуба и удаление отзыва", () -> {
            clubPage.openPageById(String.valueOf(createdClubId));
            reviewPage.deleteButton();
        });

        step("UI: Проверка исчезновения текста отзыва со страницы", () ->
                reviewPage.shouldNotHaveReviewWithText(content)
        );

        step("API: Проверка, что отзыв физически удален (ожидаем 404)", () -> {
            var response = api.reviews.getReviewWithSpec(createdReviewId, reviewsResponse404Spec);
            assertThat(response.path("detail").toString()).isEqualTo(errorBookReview);
        });
    }

    @Test
    @Description("Пользователь А создает клуб. Пользователь Б регистрируется, вступает в клуб и оставляет отзыв. " +
            "Проверяется отображение отзыва от имени Пользователя Б.")
    @DisplayName("[UI] Участник может оставить отзыв на чужой клуб")
    @Tags({@Tag("ui"), @Tag("smoke"), @Tag("positive")})
    @Severity(SeverityLevel.CRITICAL)
    public void createReviewInForeignClubFromUi() {

        SuccessfulLoginResponseModel loginA = step("Регистрация и логин Пользователя А (Владелец клуба)",
                () -> clubPage.openBlankPageWithNewUser()
        );
        accessToken = loginA.access();

        models.clubs.ClubModel club = step("Создание клуба Пользователем А через API",
                () -> api.clubs.createClub(accessToken, uniqueClubBody())
        );
        createdClubId = club.id();
        String contentClub = club.description();
        String authorsClub = club.bookAuthors();
        String titleClub = club.bookTitle();

        step("Регистрация и логин Пользователя Б (Будущий автор отзыва)", () -> {
            SuccessfulRegistrationResponseModel regB = api.users.register(new RegistrationBodyModel(newUsername, passwordDef));
            SuccessfulLoginResponseModel loginB = api.auth.login(new LoginBodyModel(newUsername, passwordDef));

            clubPage.openPageWithUser(regB.id(), newUsername, loginB.access(), loginB.refresh());
        });

        ReviewBodyModel reviewData = uniqueReviewBody();
        String reviewText = reviewData.review();
        int assessment = reviewData.assessment();
        int readPages = reviewData.readPages();

        step("UI: Вступление в клуб", () ->
                clubPage.openPageById(String.valueOf(createdClubId))
                        .pressJoinButton()
                        .verifyClub(titleClub, authorsClub, contentClub)
                        .pressAddReviewButton()
        );

        step("UI: открытие формы и отправка отзыва", () ->
                reviewPage.fillReviewForm(assessment, readPages, reviewText)
                        .submitReview()
        );

        step("UI: Проверка отображения отзыва от имени нового пользователя", () ->
                reviewPage.verifyReview(newUsername, reviewText)
        );

        step("API: Проверка корректного сохранения отзыва в БД", () -> {
            ReviewsListResponseModel reviews = api.reviews.getReviewsByClub(createdClubId, 1, 100);

            assertThat(reviews.count())
                    .as("должен быть ровно 1 отзыв")
                    .isEqualTo(1);
            ReviewResponseModel created = reviews.results().get(0);
            assertThat(created.review()).isEqualTo(reviewText);
            assertThat(created.assessment()).isEqualTo(assessment);
            assertThat(created.readPages()).isEqualTo(readPages);
            assertThat(created.club()).isEqualTo(createdClubId);
        });
    }
}