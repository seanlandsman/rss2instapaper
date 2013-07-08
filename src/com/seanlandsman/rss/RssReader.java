package com.seanlandsman.rss;

import com.seanlandsman.persistance.Store;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class RssReader {
    private Store store;

    public RssReader(Store store) {
        assert store == null : "Store cannot be null";

        this.store = store;
    }

    public List<SyndEntry> getUnreadArticleUrls(InputStream inputstream) throws IOException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(inputstream));

        //noinspection unchecked
        List<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();
        entries = removeReadEntries(entries);

        return entries;
    }

    private List<SyndEntry> removeReadEntries(List<SyndEntry> entries) {
        ArrayList<SyndEntry> filteredEntries = new ArrayList<SyndEntry>(entries);

        Iterator<SyndEntry> iterator = filteredEntries.iterator();
        while (iterator.hasNext()) {
            SyndEntry entry = iterator.next();

            if (store.read(entry.getLink()) != null) {
                iterator.remove();
            }
        }
        return filteredEntries;
    }

    public void markAsRead(String uri) {
        store.write(uri, "read");
    }

    public List<SyndEntry> getUnreadArticleUrls(String rssStream) throws URISyntaxException, IOException, FeedException {
        performHouseCleaning();

        return getUnreadArticleUrls(new URL(rssStream).openStream());
    }

    private void performHouseCleaning() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DAY_OF_MONTH, -10);
        store.deleteEntriesOlderThan(instance.getTime());
    }
}
