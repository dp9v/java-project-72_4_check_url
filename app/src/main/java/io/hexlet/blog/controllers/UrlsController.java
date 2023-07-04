package io.hexlet.blog.controllers;

import io.hexlet.blog.domain.Url;
import io.hexlet.blog.domain.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;

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

        ctx.attribute("urls", urls);
        ctx.render("urls/list.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
            .id.equalTo(id)
            .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/item.html");
    };


}
