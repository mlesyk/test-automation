package org.mlesyk.automation.config;

import org.aeonbits.owner.ConfigFactory;

public class ConfigManager {

    private static Configuration config;

    static {
        // Set default environment if not specified
        String environment = System.getProperty("environment", "dev");
        System.setProperty("environment", environment);

        config = ConfigFactory.create(Configuration.class);
    }

    public static Configuration getConfig() {
        return config;
    }

    public static String getEnvironment() {
        return System.getProperty("environment", "dev");
    }

    public static void setEnvironment(String environment) {
        System.setProperty("environment", environment);
        // Reload configuration
        config = ConfigFactory.create(Configuration.class);
    }
}