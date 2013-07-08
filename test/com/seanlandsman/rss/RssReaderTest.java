package com.seanlandsman.rss;

import com.seanlandsman.persistance.InMemoryStore;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.FeedException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RssReaderTest {
    @Before

    @Test
    public void testReadOfFeedReturnsExpectedResults() throws IOException, FeedException {
        // given
        FileInputStream testInput = getTestRssStream();

        // when
        List<SyndEntry> unreadArticleUrls = new RssReader(new InMemoryStore()).getUnreadArticleUrls(testInput);

        // expect
        assertNotNull("Returned collection of articles should not be null", unreadArticleUrls);
        assertEquals("There should have been 50 articles returned", 50, unreadArticleUrls.size());

        SyndEntry firstArticle = unreadArticleUrls.get(0);
        assertEquals("The first articles URL is not what was expected",
                "http://go.theregister.com/feed/www.theregister.co.uk/2013/07/03/oracle_enterprise_manager_12c_r3/",
                firstArticle.getLink());
        assertEquals("The first articles Title is not what was expected",
                "Oracle tunes up Enterprise Manager control freak for 12c database",
                firstArticle.getTitle());
        assertEquals("The first articles URI is not what was expected",
                "tag:theregister.co.uk,2005:story/2013/07/03/oracle_enterprise_manager_12c_r3/",
                firstArticle.getUri());
    }

    @Test
    public void testThatArticleMarkedAsReadIsNotReturnedAgain() throws IOException, FeedException {
        // given
        String uri = "http://go.theregister.com/feed/www.theregister.co.uk/2013/07/03/oracle_enterprise_manager_12c_r3/";

        // when
        RssReader rssReader = new RssReader(new InMemoryStore());
        rssReader.markAsRead(uri);
        List<SyndEntry> unreadArticleUrls = rssReader.getUnreadArticleUrls(getTestRssStream());

        // expect
        assertEquals("There should now be 49 unread articles", 49, unreadArticleUrls.size());

    }

    private FileInputStream getTestRssStream() throws FileNotFoundException {
        return new FileInputStream("./test/resources/rss.xml");
    }
}
