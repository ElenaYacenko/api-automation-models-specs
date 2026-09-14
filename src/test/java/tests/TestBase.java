package tests;

import allure.Attach;
import api.ApiClient;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.remote.DesiredCapabilities;
import pages.ClubPage;
import pages.LoginPage;
import pages.ProfilePage;
import pages.ReviewPage;

import java.util.Map;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.webdriver;

public class TestBase {

    Faker faker = new Faker();
    protected static final ApiClient api = new ApiClient();

    ClubPage clubPage = new ClubPage();
    ReviewPage reviewPage = new ReviewPage();
    LoginPage loginPage = new LoginPage();
    ProfilePage profilePage = new ProfilePage();

    @BeforeAll
    public static void setUp() {
        //  RestAssured.baseURI = "http://127.0.0.1:8000";
        RestAssured.baseURI = "https://book-club.qa.guru";
        RestAssured.basePath = "/api/v1";

        Configuration.baseUrl = "https://book-club.qa.guru";
        Configuration.browserSize = "1920x1080";

        String remoteUrl = System.getProperty("remoteUrl");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            Configuration.remote = remoteUrl;
        }
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserVersion = System.getProperty("browserVersion", "");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        Configuration.browserSize = System.getProperty("browserSize", "1920x1080");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("selenoid:options", Map.of(
                "enableVNC", true,
                "enableVideo", true
        ));
        Configuration.browserCapabilities = capabilities;
    }

    @BeforeEach
    void addListener() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterEach
    void addAttachments() {
        if(webdriver().driver().hasWebDriverStarted()) {
            Attach.screenshotAs("Last screenshot");
            Attach.pageSource();
            Attach.browserConsoleLogs();
//          Attach.addVideo();
            closeWebDriver();
        }
    }
}