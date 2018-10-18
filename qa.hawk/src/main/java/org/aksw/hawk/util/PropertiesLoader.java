package org.aksw.hawk.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final String PROPERTIES_FILENAME = "application.properties";

    public static Properties loadProperties() {

        Properties configuration = new Properties();
        try {

            InputStream inputStream = PropertiesLoader.class
                    .getClassLoader()
                    .getResourceAsStream(PROPERTIES_FILENAME);
            configuration.load(inputStream);
            inputStream.close();
        }catch(IOException e) {
            // Should not happen
        }
        return configuration;
    }
}
