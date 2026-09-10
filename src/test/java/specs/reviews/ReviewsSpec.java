package specs.reviews;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;

public class ReviewsSpec {

    public static ResponseSpecification reviewsResponse200Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/review_response_schema.json"))
            .build();

    public static ResponseSpecification reviewsResponse200ListSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/reviews_list_response_schema.json"))
            .build();

    public static ResponseSpecification reviewsResponse201Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/review_response_schema.json"))
            .expectBody("id", notNullValue())
            .build();

    public static ResponseSpecification reviewsResponse204Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(204)
            .build();

    public static ResponseSpecification reviewsResponse400Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .build();

    public static ResponseSpecification reviewsResponse401Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody("detail", notNullValue())
            .build();

    public static ResponseSpecification reviewsResponse403Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(403)
            .expectBody("detail", notNullValue())
            .build();

    public static ResponseSpecification reviewsResponse404Spec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(404)
            .expectBody("detail", notNullValue())
            .build();
}