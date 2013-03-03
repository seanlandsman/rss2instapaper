package com.seanlandsman.reader2paper;

import com.seanlandsman.instapaper.InstapaperService;
import com.seanlandsman.reader.Config;
import com.seanlandsman.reader.GoogleReader;
import com.seanlandsman.reader.GoogleReaderException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class ReaderToInstapaperService {
    private static final Logger log = Logger.getLogger(ReaderToInstapaperService.class.getName());
    private final GoogleReader googleReader;
    private final InstapaperService instapaperService;

    public ReaderToInstapaperService() throws GoogleReaderException {
        googleReader = new GoogleReader();
        instapaperService = new InstapaperService();
    }

    public void processRssFeedsAndSendToInstapaper() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        log.info("about to processRssFeedsAndSendToInstapaper");
        Map<String, String> unreadItems = googleReader.getUnreadItems(Config.getInstance().getInteger(Config.READER_STREAMS_TO_MONITOR));
        for (String idTag : unreadItems.keySet()) {
            log.info("Found idTag: " + idTag);
            instapaperService.addItemToInstapaper(unreadItems.get(idTag));
            googleReader.markItemAsRead(idTag);
        }
    }
}
