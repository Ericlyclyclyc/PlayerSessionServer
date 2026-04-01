package org.lyc122.dev.playersessionserver;

import org.lyc122.dev.playersessionserver.install_tools.ConfigExtractor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;

@SpringBootApplication
public class PlayerSessionServerApplication {

    public static void main(String[] args) {
        InitializeConfigurationFiles();

        SpringApplication.run(PlayerSessionServerApplication.class, args);
    }

    private static void InitializeConfigurationFiles() {
        boolean config_exists = true;
        File file = new File("./config/application.properties");
        if (!file.exists()) {
            config_exists = false;
        }
        try {
            ConfigExtractor.extractConfigFromJar(
                    "/app-install/application.properties.pre",
                    "/config",
                    "application.properties");
        } catch (IOException e) {
            throw new RuntimeException("Cannot extract config from jar, the application is shutting down now.",e);
        }
        if (!config_exists) {
            System.out.println("The application has been initialized.");
            System.out.println("Please configure the config file(/config/application.properties) and restart the application.");
            System.exit(0);
        }
    }

}
