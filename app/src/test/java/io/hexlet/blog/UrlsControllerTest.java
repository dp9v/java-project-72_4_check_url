package io.hexlet.blog;

import io.hexlet.blog.domain.query.QUrl;
import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UrlsControllerTest extends BaseTest {

    @Test
    void testGetList() {
        var response = Unirest
            .get(baseUrl + "/urls")
            .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("https://en.hexlet.io");
    }

    @Test
    void testShow() {
        var response = Unirest
            .get(baseUrl + "/urls/" + 1)
            .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("https://en.hexlet.io");
    }

    @Test
    void testShowNotFound() {
        var response = Unirest
            .get(baseUrl + "/urls/" + 404)
            .asString();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void testCreate() {
        String inputUrl = "https://hexlet.io.2";
        var response = Unirest.post(baseUrl + "/urls")
            .field("url", inputUrl)
            .asString();

        assertThat(response.getStatus()).isEqualTo(302);

        validateFlashMessage("Страница успешно добавлена");

        var createdUrl = new QUrl().name
            .equalTo(inputUrl)
            .findOne();

        assertThat(createdUrl).isNotNull();
        assertThat(createdUrl).matches((u) -> u.getName().equals(inputUrl), "Created URL");
    }

    @Test
    void testCreateBadUrlFormat() {
        var response = Unirest.post(baseUrl + "/urls")
            .field("url", "aashexlet.io.2")
            .asString();
        assertThat(response.getStatus()).isEqualTo(302);

        validateFlashMessage("Некорректный URL");
    }

    @Test
    void testCreateUrlExists() {
        var response = Unirest.post(baseUrl + "/urls")
            .field("url", "https://en.hexlet.io")
            .asString();
        assertThat(response.getStatus()).isEqualTo(302);

        validateFlashMessage("Страница уже существует");
    }

    private void validateFlashMessage(String expectedMessage) {
        var response = Unirest
            .get(baseUrl + "/urls")
            .asString();
        assertThat(response.getBody()).contains(expectedMessage);
    }

}
