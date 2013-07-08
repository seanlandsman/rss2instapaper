package com.seanlandsman.instapaper;

import com.seanlandsman.config.Config;
import com.seanlandsman.config.ConfigurationConstants;
import com.seanlandsman.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public class InstapaperService {
    private static final Logger log = Logger.getLogger(InstapaperService.class.getName());

    private final static String BASE_INSTAPAPER_ADD_URL = "https://www.instapaper.com/api/add?username=%s&password=%s&url=%s";

    /**
     * sends the specified url to instapaper
     *
     * @param url the url to add
     * @throws java.io.IOException
     */
    public void addItemToInstapaper(String url) throws IOException {
        String addUrl = String.format(BASE_INSTAPAPER_ADD_URL,
                Config.getInstance().get(ConfigurationConstants.INSTAPAPER_EMAIL),
                Config.getInstance().get(ConfigurationConstants.INSTAPAPER_PASSWORD),
                url);
        log.info("Result of adding to instapaper:" + openConnectionAndReturnOutput(addUrl));
    }

    public String openConnectionAndReturnOutput(String url) throws IOException {
        URL queryURL = new URL(url);
        URLConnection queryURLConnection = queryURL.openConnection();
        InputStream inputStream = queryURLConnection.getInputStream();
        String output = IOUtils.toString(inputStream);
        inputStream.close();
        return output;

    }

}
