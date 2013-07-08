package com.seanlandsman.config;

import com.seanlandsman.rss.RssReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config extends Properties {
    private static final Logger log = Logger.getLogger(Config.class.getName());

    public static final String DELIMITER = ",";

    private static Config instance;

    protected Config() {
    }

    protected void loadProperties(String propertiesFile) throws IOException {
        log.log(Level.INFO, String.format("Loading the config file"));
        InputStream resourceAsStream = getInputStream(propertiesFile);
        if (resourceAsStream == null) {
            log.log(Level.SEVERE, String.format(String.format("Could not load %s - could not find the file", ConfigurationConstants.DEFAULT_PROPERTIES_FILE)));
            throw new FileNotFoundException(String.format("Could not load %s - could not find the file", ConfigurationConstants.DEFAULT_PROPERTIES_FILE));
        }
        load(resourceAsStream);
    }

    protected InputStream getInputStream(String propertiesFile) {
        return RssReader.class.getResourceAsStream(propertiesFile);
    }

    public static synchronized Config getInstance() {
        if (instance == null) {
            try {
                instance = new Config();
                instance.loadProperties(ConfigurationConstants.DEFAULT_PROPERTIES_FILE);
            } catch (IOException e) {
                // a fatal exception - the rest of the app wont work without this
                log.log(Level.SEVERE, String.format("could not load the config file"), e);
                throw new RuntimeException(String.format("could not load the config file"), e);
            }
        }
        return instance;
    }

    public String[] getArray(String propertyName) {
        String value = getProperty(propertyName);
        if (value != null) {
            return value.split(DELIMITER);
        }
        return new String[0];
    }
}
