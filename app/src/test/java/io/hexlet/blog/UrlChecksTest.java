package io.hexlet.blog;

import io.hexlet.blog.domain.query.QUrl;
import io.hexlet.blog.domain.query.QUrlCheck;
import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UrlChecksTest extends BaseTest {

    @Test
    public void testAddCheckSuccess() {
        String url = mockWebServer.url("/")
                .toString()
                .replaceAll("/$", "");

        Unirest.post(baseUrl + "/urls")
                .field("url", url)
                .asEmpty();

        var createdUrl = new QUrl().
                name.equalTo(url)
                .findOne();

        Unirest.post(baseUrl + "/urls/" + createdUrl.getId() + "/checks")
                .asEmpty();

        var response = Unirest
                .get(baseUrl + "/urls/" + createdUrl.getId())
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        var createdUrlCheck = new QUrlCheck()
                .url.equalTo(createdUrl)
                .orderBy()
                .createdAt.desc()
                .findOne();

        assertThat(createdUrlCheck).isNotNull();
        assertThat(createdUrlCheck.getStatusCode()).isEqualTo(200);
        assertThat(createdUrlCheck.getTitle()).isEqualTo("Test title");
        assertThat(createdUrlCheck.getH1()).isEqualTo("Test header one");
        assertThat(createdUrlCheck.getDescription()).contains("test description");
    }
}
