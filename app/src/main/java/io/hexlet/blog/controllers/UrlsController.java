package io.hexlet.blog.controllers;

import io.hexlet.blog.domain.Url;
import io.hexlet.blog.domain.UrlCheck;
import io.hexlet.blog.domain.query.QUrl;
import io.hexlet.blog.domain.query.QUrlCheck;
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

        Url url = new QUrl()
            .name.equalTo(normalizedUrl)
            .findOne();

        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
        } else {
            Url newUrl = new Url(normalizedUrl);
            newUrl.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }

        ctx.redirect("/urls");
    };

    public static Handler getList = ctx -> {
        List<Url> urls = new QUrl().findList();

        Map<Long, UrlCheck> urlChecks = new QUrlCheck()
            .url.id.asMapKey()
            .orderBy()
            .createdAt.desc()
            .findMap();

        ctx.attribute("urls", urls);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("urls/list.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
            .id.equalTo(id)
            .urlChecks.fetch()
            .orderBy()
            .urlChecks.createdAt.desc()
            .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/item.html");
    };

    public static Handler checkUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
            .id.equalTo(id)
            .findOne();

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

            var newUrlCheck = new UrlCheck(statusCode, title, h1, description);
            url.getUrlChecks().add(newUrlCheck);
            url.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + url.getId());
    };


}
