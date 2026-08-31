package tests;

import api.ApiClient;
import io.restassured.RestAssured;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeAll;

public class TestBase {

    Faker faker = new Faker();
    protected static final ApiClient api = new ApiClient();

    @BeforeAll
    public static void setUp() {
      //  RestAssured.baseURI = "http://127.0.0.1:8000";
        RestAssured.baseURI = "https://book-club.qa.guru";
    }
}