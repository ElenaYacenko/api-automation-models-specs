package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import models.reviews.*;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.baseRequestSpec;
import static specs.reviews.ReviewsSpec.*;

public class ReviewsApiClient {

    @Step("[API] Получение списка отзывов клуба (пагинация)")
    public ReviewsListResponseModel getReviewsByClub(Integer clubId, int page, int pageSize) {
        return given(baseRequestSpec)
                .queryParam("club", clubId)
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .when()
                .get("/clubs/reviews/")
                .then()
                .spec(reviewsResponse200ListSpec)
                .extract()
                .as(ReviewsListResponseModel.class);
    }

    @Step("[API] Получение отзыва по id")
    public ReviewResponseModel getReview(Integer reviewId) {
        return given(baseRequestSpec)
                .pathParam("id", reviewId)
                .when()
                .get("/clubs/reviews/{id}/")
                .then()
                .spec(reviewsResponse200Spec)
                .extract()
                .as(ReviewResponseModel.class);
    }

    @Step("[API] Создание отзыва")
    public ReviewResponseModel createReview(String accessToken, ReviewBodyModel body) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .when()
                .post("/clubs/reviews/")
                .then()
                .spec(reviewsResponse201Spec)
                .extract()
                .as(ReviewResponseModel.class);
    }

    @Step("[API] Полное обновление отзыва (PUT)")
    public ReviewResponseModel updateReviewPut(String accessToken, Integer reviewId, ReviewBodyModel body) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", reviewId)
                .body(body)
                .when()
                .put("/clubs/reviews/{id}/")
                .then()
                .spec(reviewsResponse200Spec)
                .extract()
                .as(ReviewResponseModel.class);
    }

    @Step("[API] Частичное обновление отзыва (PATCH)")
    public ReviewResponseModel updateReviewPatch(String accessToken, Integer reviewId, ReviewPatchBodyModel body) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", reviewId)
                .body(body)
                .when()
                .patch("/clubs/reviews/{id}/")
                .then()
                .spec(reviewsResponse200Spec)
                .extract()
                .as(ReviewResponseModel.class);
    }

    @Step("[API] Удаление отзыва")
    public void deleteReview(String accessToken, Integer reviewId) {
        given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", reviewId)
                .when()
                .delete("/clubs/reviews/{id}/")
                .then()
                .spec(reviewsResponse204Spec);
    }

    @Step("[API] Получение отзыва по id с кастомной спецификацией")
    public Response getReviewWithSpec(Integer reviewId, ResponseSpecification spec) {
        return given(baseRequestSpec)
                .pathParam("id", reviewId)
                .when()
                .get("/clubs/reviews/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Создание отзыва  с кастомной спецификацией")
    public Response createReviewWithSpec(String accessToken, Object body, ResponseSpecification spec) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .when()
                .post("/clubs/reviews/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Полное обновление отзыва (PUT) с кастомной спецификацией")
    public Response updateReviewPutWithSpec(String accessToken,
                                            Integer reviewId,
                                            ReviewBodyModel body,
                                            ResponseSpecification spec) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", reviewId)
                .body(body)
                .when()
                .put("/clubs/reviews/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Удаление отзыва с кастомной спецификацией")
    public Response deleteReviewWithSpec(String accessToken, Integer reviewId,
                                         ResponseSpecification spec) {
        return given(baseRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", reviewId)
                .when()
                .delete("/clubs/reviews/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }
}