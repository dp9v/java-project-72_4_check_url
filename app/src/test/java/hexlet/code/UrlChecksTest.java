package hexlet.code;

import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlsRepository;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UrlChecksTest extends BaseTest {

    @SneakyThrows
    @Test
    public void testAddCheckSuccess() {
        String url = mockWebServer.url("/")
                .toString()
                .replaceAll("/$", "");

        Unirest.post(baseUrl + "/urls")
                .field("url", url)
                .asEmpty();

        var createdUrl = UrlsRepository.findByName(url).orElse(null);

        Unirest.post(baseUrl + "/urls/" + createdUrl.getId() + "/checks")
                .asEmpty();

        var response = Unirest
                .get(baseUrl + "/urls/" + createdUrl.getId())
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        var createdUrlCheck = UrlChecksRepository.findLatestChecks().get(createdUrl.getId());

        assertThat(createdUrlCheck).isNotNull();
        assertThat(createdUrlCheck.getStatusCode()).isEqualTo(200);
        assertThat(createdUrlCheck.getTitle()).isEqualTo("Test title");
        assertThat(createdUrlCheck.getH1()).isEqualTo("Test header one");
        assertThat(createdUrlCheck.getDescription()).contains("test description");
    }
}
