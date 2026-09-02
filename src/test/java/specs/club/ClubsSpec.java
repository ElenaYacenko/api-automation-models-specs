package specs.clubs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;

public class ClubsSpec {

    public static RequestSpecification clubsRequestSpec = with()
            .log().all()
            .basePath("/api/v1")
            .contentType(JSON);

    public static ResponseSpecification clubsResponse200Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .build();

    public static ResponseSpecification clubsResponse201Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath("schemas/club/club_response_schema.json"))
            .expectBody("id", notNullValue())
            .build();

    public static ResponseSpecification clubsResponse204Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(204)
            .build();

    public static ResponseSpecification clubsResponse400Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody("bookTitle", notNullValue())
            .build();

    public static ResponseSpecification clubsResponse401Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody("detail", notNullValue())
            .build();

    public static ResponseSpecification clubsResponse404Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(404)
            .expectBody("detail", notNullValue())
            .build();
}