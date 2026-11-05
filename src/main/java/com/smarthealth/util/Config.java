package com.smarthealth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads app.properties; an environment variable (KEY_NAME) overrides any key (key.name). */
public final class Config {
    private static final Properties P = new Properties();
    static {
        try (InputStream in = Config.class.getResourceAsStream("/app.properties")) {
            if (in != null) P.load(in);
        } catch (IOException ignored) { }
    }
    private Config() {}

    public static String get(String key, String def) {
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isEmpty()) return env;
        String v = P.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
