package com.seanlandsman.reader2paper;

import com.seanlandsman.config.Config;
import com.seanlandsman.config.ConfigurationConstants;
import com.seanlandsman.instapaper.InstapaperService;
import com.seanlandsman.persistance.GoogleDataStore;
import com.seanlandsman.rss.RssReader;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.FeedException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class RssToInstapaperService {
    private static final Logger log = Logger.getLogger(RssToInstapaperService.class.getName());
    private final InstapaperService instapaperService;
    private final RssReader rssReader;

    public RssToInstapaperService() {
        rssReader = new RssReader(new GoogleDataStore());
        instapaperService = new InstapaperService();
    }

    public void processRssFeedsAndSendToInstapaper() throws FeedException, IOException, URISyntaxException {
        log.info("about to processRssFeedsAndSendToInstapaper");
        String[] rssStreams = Config.getInstance().getArray(ConfigurationConstants.READER_STREAMS_TO_MONITOR);
        for (String rssStream : rssStreams) {
            List<SyndEntry> unreadArticleUrls = rssReader.getUnreadArticleUrls(rssStream);
            log.info(String.format("Found %d articles from feed %s", unreadArticleUrls.size(), rssStream));
            for (SyndEntry unreadArticleUrl : unreadArticleUrls) {
                String uri = unreadArticleUrl.getLink();
                log.info("Found url: " + uri);
                instapaperService.addItemToInstapaper(uri);
                rssReader.markAsRead(uri);
            }
        }
    }
}
