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

        Configuration.baseUrl = System.getProperty("url", "https://book-club.qa.guru");
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserVersion = System.getProperty("browserVersion", "");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        Configuration.browserSize = System.getProperty("browserSize", "1920x1080");

        String remote = System.getProperty("remote");
        if (remote != null && !remote.isEmpty()) {
            Configuration.remote = remote;
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("selenoid:options", Map.<String, Object>of(
                    "enableVNC", true,
                    "enableVideo", true,
                    "enableLog", true
            ));
            Configuration.browserCapabilities = capabilities;
    }
        // Логирование для проверки
        System.out.println("URL: " + Configuration.baseUrl);
        System.out.println("Browser: " + Configuration.browser);
        System.out.println("Browser size: " + Configuration.browserSize);
        System.out.println("Remote: " + Configuration.remote);
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