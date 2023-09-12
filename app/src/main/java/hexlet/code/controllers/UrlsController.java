package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.repository.UrlChecksRepository;
import hexlet.code.repository.UrlsRepository;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class UrlsController {
    public static Handler createUrl = ctx -> {
        String inputUrl = ctx.formParam("url");

        URL parsedUrl;
        try {
            parsedUrl = new URL(inputUrl);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String normalizedUrl = String
            .format(
                "%s://%s%s",
                parsedUrl.getProtocol(),
                parsedUrl.getHost(),
                parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()
            )
            .toLowerCase();


        var url = UrlsRepository.findByName(normalizedUrl);

        if (url.isPresent()) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
        } else {
            Url newUrl = new Url(normalizedUrl);
            UrlsRepository.save(newUrl);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }

        ctx.redirect("/urls");
    };

    public static Handler getList = ctx -> {
        List<Url> urls = UrlsRepository.getEntities();
        Map<Long, UrlCheck> urlChecks = UrlChecksRepository.findLatestChecks();

        ctx.attribute("urls", urls);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("urls/list.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        var url = UrlsRepository.find(id)
            .orElseThrow(() -> new NotFoundResponse("Url with id = " + id + " not found"));

        var urlChecks = UrlChecksRepository.findByUrlId(id);

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("urls/item.html");
    };

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlsRepository.findById(id)
            .orElseThrow(() -> new NotFoundResponse("Url with id = " + id + " not found"));


        if (url == null) {
            throw new NotFoundResponse();
        }

        try {
            var response = Unirest.get(url.getName()).asString();
            var doc = Jsoup.parse(response.getBody());

            var statusCode = response.getStatus();
            var title = doc.title();
            var firstH1 = doc.selectFirst("h1");
            var h1 = firstH1 == null ? "" : firstH1.text();
            var descriptionElement = doc.selectFirst("meta[name=description]");
            var description = descriptionElement == null ? "" : descriptionElement.attr("content");

            UrlCheck newUrlCheck = new UrlCheck(statusCode, title, h1, description);
            newUrlCheck.setUrlId(id);
            UrlChecksRepository.save(newUrlCheck);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + url.getId());
    };


}
