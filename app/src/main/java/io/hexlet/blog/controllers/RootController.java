package io.hexlet.blog.controllers;

import io.javalin.http.Handler;

public class RootController {

    public static Handler welcome = ctx -> {
        ctx.render("index.html");
    };
}
