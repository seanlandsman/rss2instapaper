package com.seanlandsman.config;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigTest {
    @Test
    public void testOpenInputStream() {
        // given
        String propertiesFile = "/resources/test.properties";

        // when
        InputStream inputStream = new Config().getInputStream(propertiesFile);

        // expect
        assertNotNull("Returned inputstream should not be null", inputStream);

        closeInputStream(inputStream);
    }

    @Test
    public void testOpenInputStreamFile() throws IOException {
        // given
        String propertiesFile = "/resources/test.properties";

        // when
        Config config = new Config();
        config.loadProperties(propertiesFile);

        // expect
        assertEquals("Could not retrieve single property",
                "email@test.com",
                config.getProperty(ConfigurationConstants.INSTAPAPER_EMAIL));
        assertEquals("Could not retrieve delimited property",
                Arrays.asList("feed1", "feed2"),
                Arrays.asList(config.getArray(ConfigurationConstants.READER_STREAMS_TO_MONITOR)));
    }

    private void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
        }
    }

}
