package io.hexlet.blog;

import io.javalin.Javalin;

public class App {

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    public static Javalin getApp() {
        return Javalin.create(config -> {
                if (!isProduction()) {
                    config.plugins.enableDevLogging();
                }
            }
        )
            .get("/", ctx -> ctx.result("Hello World"))
            .before(ctx -> ctx.attribute("ctx", ctx));
    }

    public static void main(String[] args) {
        getApp().start();
    }
}
