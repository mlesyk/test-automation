package org.mlesyk.automation.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;

@LoadPolicy(LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "classpath:config/${environment}.properties",
        "classpath:config/default.properties"
})
public interface Configuration extends Config {

    @Key("base.url")
    @DefaultValue("https://jsonplaceholder.typicode.com")
    String baseUrl();

    @Key("wiremock.url")
    @DefaultValue("http://localhost:8080")
    String wiremockUrl();

    @Key("timeout.request")
    @DefaultValue("30")
    int requestTimeout();

    @Key("timeout.response")
    @DefaultValue("30")
    int responseTimeout();

    @Key("retry.count")
    @DefaultValue("3")
    int retryCount();

    @Key("parallel.threads")
    @DefaultValue("5")
    int parallelThreads();

    // Authentication settings
    @Key("auth.jwt.secret")
    @DefaultValue("mySecretKey")
    String jwtSecret();

    @Key("auth.jwt.expiration")
    @DefaultValue("3600")
    int jwtExpiration();

    @Key("auth.basic.username")
    @DefaultValue("admin")
    String basicAuthUsername();

    @Key("auth.basic.password")
    @DefaultValue("password")
    String basicAuthPassword();

    // Database settings (for future integration)
    @Key("db.url")
    @DefaultValue("jdbc:h2:mem:testdb")
    String databaseUrl();

    @Key("db.username")
    @DefaultValue("sa")
    String databaseUsername();

    @Key("db.password")
    @DefaultValue("")
    String databasePassword();

    // Reporting settings
    @Key("allure.results.directory")
    @DefaultValue("target/allure-results")
    String allureResultsDirectory();

    @Key("screenshots.enabled")
    @DefaultValue("true")
    boolean screenshotsEnabled();
}