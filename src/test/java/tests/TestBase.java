package tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class TestBase {
    @BeforeAll
    public static void setUp() {
      //  RestAssured.baseURI = "http://127.0.0.1:8000";
        RestAssured.baseURI = "https://book-club.qa.guru";
    }
}