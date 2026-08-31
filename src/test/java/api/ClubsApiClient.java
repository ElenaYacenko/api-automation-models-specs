package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import models.clubs.ClubBodyModel;
import models.clubs.ClubModel;
import models.clubs.ClubPatchBodyModel;
import models.clubs.ClubsListResponseModel;
import specs.clubs.ClubsSpec;

import static io.restassured.RestAssured.given;
import static specs.clubs.ClubsSpec.clubsRequestSpec;
import static specs.clubs.ClubsSpec.clubsResponse201Spec;

public class ClubsApiClient {

    @Step("[API] Получение списка клубов")
    public ClubsListResponseModel getClubs() {
        return given(clubsRequestSpec)
                .when()
                .get("/clubs/")
                .then()
                .spec(ClubsSpec.clubsResponse200Spec)
                .extract()
                .as(ClubsListResponseModel.class);
    }

    @Step("[API] Получение списка клубов с фильтрацией")
    public ClubsListResponseModel getClubsWithFilter(String filter, String value) {
        return given(clubsRequestSpec)
                .queryParam(filter, value)
                .when()
                .get("/clubs/")
                .then()
                .spec(ClubsSpec.clubsResponse200Spec)
                .extract()
                .as(ClubsListResponseModel.class);
    }

    @Step("[API] Получение списка клубов с пагинацией")
    public ClubsListResponseModel getClubsWithPagination(int limit, int offset) {
        return given(clubsRequestSpec)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .when()
                .get("/clubs/")
                .then()
                .spec(ClubsSpec.clubsResponse200Spec)
                .extract()
                .as(ClubsListResponseModel.class);
    }

    @Step("[API] Получение клуба по id")
    public ClubModel getClub(Integer clubId) {
        return given(clubsRequestSpec)
                .pathParam("id", clubId)
                .when()
                .get("/clubs/{id}/")
                .then()
                .spec(ClubsSpec.clubsResponse200Spec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("[API] Получение клуба по id с кастомной спецификацией")
    public Response getClubWithSpec(Integer clubId, ResponseSpecification spec) {
        return given(clubsRequestSpec)
                .pathParam("id", clubId)
                .when()
                .get("/clubs/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Создание клуба")
    public ClubModel createClub(String accessToken, ClubBodyModel body) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .body(body)
                .when()
                .post("/clubs/")
                .then()
                .spec(clubsResponse201Spec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("[API] Создание клуба с кастомной спецификацией")
    public Response createClubWithSpec(String accessToken, ClubBodyModel body, ResponseSpecification spec) {
        var request = given(clubsRequestSpec);
        if (accessToken != null && !accessToken.isEmpty()) {
            request.auth().oauth2(accessToken);
        }
        return request
                .body(body)
                .when()
                .post("/clubs/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Полное обновление клуба")
    public ClubModel updateClub(String accessToken, Integer clubId, ClubBodyModel body) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", clubId)
                .body(body)
                .when()
                .put("/clubs/{id}/")
                .then()
                .spec(ClubsSpec.clubsResponse200Spec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("[API] Полное обновление клуба с кастомной спецификацией")
    public Response updateClubWithSpec(String accessToken, Integer clubId, ClubBodyModel body, ResponseSpecification spec) {
        var request = given(clubsRequestSpec);
        if (accessToken != null && !accessToken.isEmpty()) {
            request.auth().oauth2(accessToken);
        }
        return request
                .pathParam("id", clubId)
                .body(body)
                .when()
                .put("/clubs/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Частичное обновление клуба")
    public ClubModel patchClub(String accessToken, Integer clubId, ClubPatchBodyModel body) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", clubId)
                .body(body)
                .when()
                .patch("/clubs/{id}/")
                .then()
                .spec(ClubsSpec.clubsResponse200Spec)
                .extract()
                .as(ClubModel.class);
    }

    @Step("[API] Частичное обновление клуба с кастомной спецификацией")
    public Response patchClubWithSpec(String accessToken, Integer clubId, ClubPatchBodyModel body, ResponseSpecification spec) {
        var request = given(clubsRequestSpec);
        if (accessToken != null && !accessToken.isEmpty()) {
            request.auth().oauth2(accessToken);
        }
        return request
                .pathParam("id", clubId)
                .body(body)
                .when()
                .patch("/clubs/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }

    @Step("[API] Удаление клуба")
    public void deleteClub(String accessToken, Integer clubId) {
        given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .pathParam("id", clubId)
                .when()
                .delete("/clubs/{id}/")
                .then()
                .spec(ClubsSpec.clubsResponse204Spec);
    }

    @Step("[API] Удаление клуба с кастомной спецификацией")
    public Response deleteClubWithSpec(String accessToken, Integer clubId, ResponseSpecification spec) {
        var request = given(clubsRequestSpec);
        if (accessToken != null && !accessToken.isEmpty()) {
            request.auth().oauth2(accessToken);
        }
        return request
                .pathParam("id", clubId)
                .when()
                .delete("/clubs/{id}/")
                .then()
                .spec(spec)
                .extract()
                .response();
    }
}