package pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class ReviewPage extends BasePage {
    private final SelenideElement reviewerTitle = $x("//div[@class='reviews-header']/h2[contains(text(),'Отзывы')]");
    private final ElementsCollection reviewCards = $$(".review-card");    private final SelenideElement reviewerName = $(".reviewer-name");
    private final SelenideElement reviewerStars = $(".stars");
    private final SelenideElement reviewerContent = $(".review-content");
    private final SelenideElement reviewerAssessment = $("#assessment");
    private final SelenideElement reviewerReadPages = $("#readPages");
    private final SelenideElement reviewerOfClub = $("#review");
    private final SelenideElement submitButton = $(".save-btn");
    private final SelenideElement cancelButton = $(".cancel-btn");
    private final SelenideElement deleteButton = $(".delete-review-btn");




    @Step("[UI] Заполнить форму отзыва: оценка={stars}, страниц={pages}, текст='{content}'")
    public ReviewPage fillReviewForm(int stars, int pages, String content) {
        reviewerAssessment.setValue(String.valueOf(stars));
        reviewerReadPages.setValue(String.valueOf(pages));
        reviewerOfClub.setValue(content);
        return this;
    }

    @Step("[UI] Нажать 'Опубликовать'")
    public ReviewPage submitReview() {
        submitButton.pressEnter();
        return this;
    }

    @Step("[UI] Нажать 'Удалить' и подтвердить диалог ")
    public ReviewPage deleteButton() {
        deleteButton.click();
        confirm();
        return this;
    }

    @Step("[UI] Проверка корректности данных отзыва: автор='{name}', текст='{content}'")
    public ReviewPage verifyReview(String name, String content) {
        reviewerTitle.shouldBe(visible);
        reviewerName.shouldHave(Condition.text(name));
        reviewerStars.shouldBe(visible);
        reviewerContent.shouldHave(Condition.text(content));
        return this;
    }

    @Step("[UI] Проверить, что отзыв с текстом '{text}' отсутствует на странице")
    public ReviewPage shouldNotHaveReviewWithText(String text) {
        reviewCards.findBy(Condition.text(text)).shouldNot(Condition.exist);
        return this;
    }
}