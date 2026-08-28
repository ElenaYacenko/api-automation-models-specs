package tests;

import net.datafaker.Faker;

import java.util.Locale;

public class TestData {

    public static final String username = "Kiersten";
    public static final String password = "Sang";
    public static final String wrongPassword = "Tempie1";
    public static final String wrongUsername = "Holly1";
    public static final String expectedTokenPath = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    public static final String expectedDetailError = "Invalid username or password.";
    public static final String expectedError = "A user with that username already exists.";
    public static final String expectedErrorPass = "This field may not be blank.";
    public static final String expectedErrorInvalidToken = "Token is invalid";
    public static final String expectedErrorIBlackList = "Token is blacklisted";
    public static final String invalidToken = "invalid.token";

    private static final Faker faker = new Faker();
    private static final Faker fakerRu = new Faker(new Locale("ru"));

    public static  final String newUsername = faker.name().lastName();
    public static  final String newFirstName = fakerRu.name().lastName();
    public static  final String newLastName = fakerRu.name().lastName();
    public static final String newEmail = faker.internet().emailAddress();
}
