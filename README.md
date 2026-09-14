# Book Club — API & UI Automation Tests

Автотесты для проекта [book-club.qa.guru](https://book-club.qa.guru):
- **API-тесты** — REST API (`/api/v1/`)
- **UI-тесты** — Selenide (гибридный подход: подготовка через API, проверка через UI)

## Стек

| Компонент | Технология |
|---|---|
| Язык | Java 17+ |
| Сборка | Gradle |
| Тест-раннер | JUnit 5 |
| API | RestAssured 6.x |
| UI | Selenide 7.x |
| JSON | Jackson |
| Данные | DataFaker |
| Ассерты | AssertJ |
| Отчёты | Allure |
| Логирование | SLF4J |

## Запуск тестов

```bash
# Все тесты (API + UI)
./gradlew clean test

# Только API-тесты
./gradlew test --tests "*ClubsTests"
./gradlew test --tests "*ReviewTests"

# Только UI-тесты
./gradlew test --tests "*ClubsUiTests"
./gradlew test --tests "*ReviewUiTests"

# По тегам
./gradlew test -DincludeTags="smoke"
./gradlew test -DincludeTags="ui"
./gradlew test -DincludeTags="security"
```
## Allure-отчёт
```bash
# Сгенерировать HTML-отчёт
./gradlew allureReport

# Открыть отчёт в браузере
./gradlew allureServe
```

![allure (2).png](images/allure%20%282%29.png)

## Покрытие
### API-тесты
Клубы (/api/v1/clubs/):
- CRUD: create, get list, get by id, put, patch, delete
- Негативные: 404, 401, 400
- Права доступа: чужой клуб → 403

Отзывы (/api/v1/clubs/reviews/):
- CRUD: create, get list, get by id, put, patch, delete
- Негативные: 404, 400 (без assessment)
- Права доступа: чужой отзыв → 403 (update, delete)

### UI-тесты (гибридные: API-подготовка + UI-проверка)
Клубы:
- createClubFromUi — создание через форму → проверка через API
- clubIsVisibleOnPage — отображение клуба
- ownerCannotLeaveClubFromUi — владелец не может покинуть клуб

Отзывы:
- reviewIsVisibleOnClubPage — отображение отзыва
- createReviewFromUi — создание через форму → проверка через API
- deleteReviewFromUi — удаление с alert → проверка через API
- createReviewInForeignClubFromUi — отзыв в чужом клубе

## Ключевые практики
* Гибридный подход: подготовка данных через API, UI проверяет отображение и клики.
* Уникальность данных: System.currentTimeMillis() + DataFaker для избежания flaky-тестов.
* Read-after-write: после создания/обновления — повторный GET для проверки консистентности.
* Page Object: селекторы инкапсулированы в Page Objects, тесты — декларативны.
* Allure @Step: каждый шаг логируется в отчёт.
* Теги: api / ui / smoke / regression / positive / negative / security — для фильтрации.
* Cleanup: @AfterEach удаляет созданные данные, даже если тест упал.

---