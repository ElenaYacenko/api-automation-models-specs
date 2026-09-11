package pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class ClubPage extends BasePage {

    private final SelenideElement leaveButton = $(".leave-btn");
    private final SelenideElement error = $(".error");
    private final SelenideElement clubName = $(".club-header");
    private final SelenideElement telegramButton = $(".telegram-btn");
    private final SelenideElement addReviewButton = $(".add-review-btn");
    private final SelenideElement authorsName =$(".authors");
    private final SelenideElement descriptionClub =$(".description");
    private final SelenideElement joinButton = $(".join-btn");
    private final SelenideElement createdButton = $("[data-testid=create-club-link]");
    private final SelenideElement bookTitleInput = $("#bookTitle");
    private final SelenideElement authorsInput = $("#bookAuthors");
    private final SelenideElement yearInput = $("#publicationYear");
    private final SelenideElement descriptionInput =  $("#description");
    private final SelenideElement telegramInput = $("#telegramChatLink");

    @Step("[UI] Открытие страницы клуба по id: {clubId}")
    public ClubPage openMainPage() {
        open("/");
        return this;
    }

    @Step("[UI] Открытие страницы клуба по id: {clubId}")
    public ClubPage openPageById(String clubId) {
        open("/clubs/" + clubId);
        return this;
    }

    @Step("[UI] Нажать «Покинуть клуб» и подтвердить диалог")
    public ClubPage pressLeaveClubButton() {
        leaveButton.click();
        confirm();
        return this;
    }

    @Step("[UI] Нажать «Написать отзыв»")
    public ClubPage pressAddReviewButton() {
        addReviewButton.click();
        return this;
    }

    @Step("[UI] Нажать «Присоединиться»")
    public ClubPage pressJoinButton() {
        joinButton.click();
        return this;
    }

    @Step("[UI] Нажать «Создать клуб»")
    public ClubPage createdClubButton() {
        createdButton.click();
        return this;
    }

    @Step("[UI] Создание клуба")
    public ClubPage createMyClub(String name, String author, String year, String description, String value) {
        bookTitleInput.setValue(name);
        authorsInput.setValue(author);
        yearInput.setValue(year);
        descriptionInput.setValue(description);
        telegramInput.setValue("https://t.me/qa_guru" + value).pressEnter();
        return this;
    }

    @Step("[UI] Владелец не может покинуть клуб: подтверждение и сообщение об ошибке")
    public ClubPage assertOwnerCannotLeaveClub() {
        error.shouldHave(text("Не удалось покинуть клуб"));
        return this;
    }

    @Step("[UI] Проверка отображения элементов")
    public ClubPage verifyClub(String name, String authors, String description) {
        clubName.shouldHave(Condition.text(name));
        telegramButton.shouldBe(visible);
        leaveButton.shouldBe(visible);
        addReviewButton.shouldBe(visible);
        authorsName.shouldHave(Condition.text(authors));
        descriptionClub.shouldHave(Condition.text(description));
        return this;
    }
}