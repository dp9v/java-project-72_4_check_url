package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.repository.UrlsRepository;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UrlsTest extends BaseTest {

    @SneakyThrows
    @Test
    public void testGetList() {
        var url = new Url("https://en.hexlet.io/asd");
        UrlsRepository.save(url);

        var response = Unirest
            .get(baseUrl + "/urls")
            .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("https://en.hexlet.io");
    }

    @SneakyThrows
    @Test
    public void testShow() {
        var url = new Url("https://en.hexlet.io/asd");
        UrlsRepository.save(url);

        var response = Unirest
            .get(baseUrl + "/urls/" + 1)
            .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("https://en.hexlet.io");
    }

    @Test
    public void testShowNotFound() {
        var response = Unirest
            .get(baseUrl + "/urls/" + 404)
            .asString();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SneakyThrows
    @Test
    public void testCreate() {
        String inputUrl = "https://hexlet.io.2";
        var response = Unirest.post(baseUrl + "/urls")
            .field("url", inputUrl)
            .asString();

        assertThat(response.getStatus()).isEqualTo(302);

        validateFlashMessage("Страница успешно добавлена");

        var createdUrl = UrlsRepository.findByName(inputUrl).orElse(null);

        AssertionsForClassTypes.assertThat(createdUrl).isNotNull();
        AssertionsForClassTypes.assertThat(createdUrl).matches((u) -> u.getName().equals(inputUrl), "Created URL");
    }

    @Test
    public void testCreateBadUrlFormat() {
        var response = Unirest.post(baseUrl + "/urls")
            .field("url", "aashexlet.io.2")
            .asString();
        assertThat(response.getStatus()).isEqualTo(302);

        validateFlashMessage("Некорректный URL");
    }
}
