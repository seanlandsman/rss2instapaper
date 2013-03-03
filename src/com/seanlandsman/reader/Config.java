package com.seanlandsman.reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config extends Properties {
    public static final String DEFAULT_PROPERTIES_FILE = "/resources/reader.properties";

    // property names
    // google reader properties
    public static final String GOOGLE_EMAIL_ADDRESS = "GOOGLE_EMAIL_ADDRESS";
    public static final String GOOGLE_PASSWORD = "GOOGLE_PASSWORD";
    public static final String GOOGLE_USER_ID = "GOOGLE_USER_ID";
    public static final String READER_MAX_UNREAD_ITEMS = "READER_MAX_UNREAD_ITEMS";
    public static final String READER_STREAMS_TO_MONITOR = "READER_STREAMS_TO_MONITOR";
    // instapaper properties
    public static final String INSTAPAPER_PASSWORD = "INSTAPAPER_PASSWORD";
    public static final String INSTAPAPER_EMAIL = "INSTAPAPER_EMAIL";

    private static final Logger log = Logger.getLogger(Config.class.getName());

    private static final Config instance;

    static {
        try {
            instance = new Config();
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("could not load the config file"), e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private Config() throws IOException {
        try {
            log.log(Level.INFO, String.format("Loading the config file"));
            InputStream resourceAsStream = GoogleReader.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE);
            if (resourceAsStream == null) {
                log.log(Level.SEVERE, String.format(String.format("Could not load %s - could not find the file", DEFAULT_PROPERTIES_FILE)));
                throw new FileNotFoundException(String.format("Could not load %s - could not find the file", DEFAULT_PROPERTIES_FILE));
            }
            load(resourceAsStream);
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("Could not load properties file [%s] due to %s", DEFAULT_PROPERTIES_FILE, e.getMessage()));
            throw e;
        }
    }

    public int getInteger(String propertyName) {
        return Integer.parseInt(getProperty(propertyName));
    }

    public static Config getInstance() {
        return instance;
    }
}
