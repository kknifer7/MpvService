package io.github.kknifer7.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Knifer
 */
public final class PropertyUtil {

    private PropertyUtil() {
        throw new AssertionError();
    }

    public static Properties load(String path) throws IOException {
        Properties properties = new Properties();

        try (InputStream in = PropertyUtil.class.getClassLoader().getResourceAsStream(path)){
            properties.load(in);
        }

        return properties;
    }
}
