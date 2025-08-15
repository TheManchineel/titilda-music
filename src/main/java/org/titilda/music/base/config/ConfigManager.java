package org.titilda.music.base.config;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class ConfigManager {
    public enum ConfigKey {
        DATABASE_URL("db.url"),
        DATABASE_USER("db.user"),
        DATABASE_PASSWORD("db.password"),
        AUTH_SECRET("auth.secret");

        private final String key;

        ConfigKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
    // File is in /opt/homebrew/Cellar/tomcat@10/10.1.42/libexec/bin (Emanuel's pc)
    public static final String CONFIG_FILE = "config.properties";
    private static Properties prop;

    public static void loadConfig() {
        if (prop != null) {
            return;
        }

        try (InputStream input = Files.newInputStream(new File(CONFIG_FILE).toPath())) {
            prop = new java.util.Properties();
            prop.load(input);
            for (ConfigKey key : ConfigKey.values()) {
                if (!prop.containsKey(key.getKey())) {
                    System.err.println("Missing configuration key: " + key.getKey());
                }
            }
        } catch (java.io.IOException ex) {
            System.out.println(System.getProperty("user.dir"));
            System.err.println("Error loading configuration file: " + CONFIG_FILE);
        }
    }

    public static String getString(ConfigKey key) {
        if (prop == null) {
            loadConfig();
        }
        return prop.getProperty(key.getKey());
    }

    public static int getInt(ConfigKey key) {
        String value = getString(key);
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            System.err.println("Invalid integer value for key: " + key + " - " + value);
            return 0;
        }
    }
}
