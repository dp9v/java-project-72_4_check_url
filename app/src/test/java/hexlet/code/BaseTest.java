package hexlet.code;

import io.javalin.Javalin;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

abstract class BaseTest {

    private static Javalin app;
    protected static String baseUrl;
    protected static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(
                new MockResponse().setBody(loadFixture("index.html"))
        );
        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
    }

    private static String loadFixture(String fileName) throws IOException {
        var path = Paths.get("src/test/resources/fixtures", fileName).toAbsolutePath().normalize();
        return Files.readString(path).trim();
    }

    protected void validateFlashMessage(String expectedMessage) {
        var response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        assertThat(response.getBody()).contains(expectedMessage);
    }
}
