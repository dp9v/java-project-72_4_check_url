package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlsController;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.rendering.template.JavalinThymeleaf;
import lombok.SneakyThrows;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    private static String getDatabaseUrl() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");
    }

    private static TemplateEngine getTemplateEngine() {
        var templateEngine = new TemplateEngine();

        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());
        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);

        app.routes(() -> {
            path("urls", () -> {
                ApiBuilder.post(UrlsController.createUrl);
                get(UrlsController.getList);
                path("{id}", () -> {
                    get(UrlsController.showUrl);
                    post("/checks", UrlsController.checkUrl);
                });
            });
        });
    }

    @SneakyThrows
    public static void initDB() {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDatabaseUrl());

        var dataSource = new HikariDataSource(hikariConfig);
        var url = App.class.getClassLoader().getResource("schema.sql");
        var file = new File(url.getFile());
        var sql = Files.lines(file.toPath())
            .collect(Collectors.joining("\n"));

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;
    }

    public static Javalin getApp() {
        initDB();
        var app = Javalin.create(config -> {
            if (!isProduction()) {
                config.plugins.enableDevLogging();
            }
            config.staticFiles.enableWebjars();
            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);
        app.before(ctx -> ctx.attribute("ctx", ctx));
        return app;
    }

    public static void main(String[] args) {
        getApp().start();
    }
}
