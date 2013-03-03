package com.seanlandsman.reader;

import com.seanlandsman.utils.IOUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleReader {
    // the google account credentials
    private String auth;
    private String token;
    private String userId;

    // if we only want to capture selected rss feeds in the reader account
    private final Set<String> streamsToMonitor = new HashSet<String>();

    private static final Logger log = Logger.getLogger(GoogleReader.class.getName());

    // google reader specific urls
    /**
     * URL used for authenticating and obtaining an authentication token.
     * More details about how it works:
     * <code>http://code.google.com/apis/accounts/AuthForInstalledApps.html<code>
     */
    private static final String AUTHENTICATION_URL = "https://www.google.com/accounts/ClientLogin";
    private static final String READING_LIST_URL = "http://www.google.com/reader/atom/user/%s/state/com.google/reading-list?n=%s&xt=user/-/state/com.google/read";
    private static final String TOKEN_URL = "http://www.google.com/reader/api/0/token";
    private static final String BASE_EDIT_URL = "http://www.google.com/reader/api/0/edit-tag?client=client=contact:";
    private static final String[] SUBJECT_WORDS_TO_IGNORE = {"apple", "iphone", "android", "steve jobs", "cupertino"};

    public GoogleReader() throws GoogleReaderException {
        initialise();
    }

    /**
     * get and store the properties we need
     */
    private void initialise() throws GoogleReaderException {
        log.log(Level.INFO, String.format("Initializing the reader"));
        userId = Config.getInstance().getProperty(Config.GOOGLE_USER_ID);
        streamsToMonitor.addAll(getStreamsToMonitor());

        String emailAddress = Config.getInstance().getProperty(Config.GOOGLE_EMAIL_ADDRESS);
        String password = Config.getInstance().getProperty(Config.GOOGLE_PASSWORD);
        authenticate(emailAddress, password);
    }

    /**
     * Retrieves and stores the authentication token for the provided set of credentials.
     * This is the  authorization token that can be used to access authenticated Google Base data API feeds
     *
     * @param email    the google email address to use
     * @param password the password for the google account
     */
    private void authenticate(String email, String password) throws GoogleReaderException {
        try {
            URL loginURL = new URL(AUTHENTICATION_URL + "?service=reader&Email=" + email + "&Passwd=" + password);
            BufferedReader loginBuffer = new BufferedReader(new InputStreamReader(loginURL.openStream()));

            // the sid and lsid - we dont need this so ignore
            String sid = loginBuffer.readLine().split("=")[1];
            String lsid = loginBuffer.readLine().split("=")[1];

            // the authorization
            auth = loginBuffer.readLine().split("=")[1];

            URL tokenURL = new URL(TOKEN_URL);
            URLConnection tokenURLConnection = tokenURL.openConnection();
            tokenURLConnection.setRequestProperty("Authorization", "GoogleLogin auth=" + auth);
            tokenURLConnection.connect();

            // store this token from the successful login - we'll use it to access the google reader service
            token = IOUtils.toString(tokenURLConnection.getInputStream());
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("Could not authenticate with credentials [%s / %s]. Error is: %s", email, password, e.getMessage()));
            throw new GoogleReaderException(String.format("Could not authenticate with credentials [%s / %s]. Error is: %s", email, password, e.getMessage()));

        }
    }

    /**
     * retrieves a set of feeds that we want to send to instapaper
     * (if not specified in the properties then all feeds will be sent, later in the code flow)
     */
    private Set<String> getStreamsToMonitor() {
        Set<String> streams = new HashSet<String>();

        String monitoredStreams = Config.getInstance().getProperty(Config.READER_STREAMS_TO_MONITOR);
        if (monitoredStreams != null && !monitoredStreams.isEmpty()) {
            String[] monitoredStream = monitoredStreams.split(",");
            streams.addAll(Arrays.asList(monitoredStream));
        }
        return streams;
    }

    public Map<String, String> getUnreadItems(final int numberOfItems) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        final String readingListURL = String.format(READING_LIST_URL, userId, numberOfItems);
        URLConnection connection = getAuthorizedConnection(readingListURL, false);

        InputStream inputStream = connection.getInputStream();
        Map<String, String> tagIDToURLs = xmlParsing(inputStream);
        inputStream.close();

        return tagIDToURLs;
    }

    public void markItemAsRead(String itemTag) throws IOException {
        Map<String, String> argMap = new HashMap<String, String>();
        argMap.put("i", itemTag);
        argMap.put("a", "user/-/state/com.google/read");
        argMap.put("ac", "edit");
        log.info("Result of marking item as read:" + postItem(argMap));
    }


    private String postItem(Map<String, String> arguments) throws IOException {
        String editUrl = BASE_EDIT_URL + Config.getInstance().get(Config.GOOGLE_EMAIL_ADDRESS);
        URLConnection queryURLConnection = getAuthorizedConnection(editUrl, true);

        // Post the data item
        if (arguments != null && !arguments.isEmpty()) {
            OutputStream outputStream = queryURLConnection.getOutputStream();

            StringBuilder builder = new StringBuilder();
            for (String key : arguments.keySet()) {
                builder.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(arguments.get(key), "UTF-8")).append("&");

            }
            builder.append(URLEncoder.encode("T", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8"));

            outputStream.write(builder.toString().getBytes("UTF-8"));
            outputStream.close();
        }

        return IOUtils.toString(queryURLConnection.getInputStream());
    }

    private URLConnection getAuthorizedConnection(String url, boolean doOutput) throws IOException {
        URL queryURL = new URL(url);
        URLConnection queryURLConnection = queryURL.openConnection();
        queryURLConnection.setDoOutput(doOutput);

        queryURLConnection.setRequestProperty("Authorization", "GoogleLogin auth=" + auth);
        queryURLConnection.connect();
        return queryURLConnection;
    }


    private Map<String, String> xmlParsing(InputStream xmlInputstream) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        Map<String, String> itemTagToURL = new LinkedHashMap<String, String>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new DefaultNamespaceContext());
        InputSource inputSource = new InputSource(xmlInputstream);
        NodeList nodes = (NodeList) xpath.evaluate("/ns:feed/ns:entry", inputSource, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String stream = xpath.evaluate("ns:source/@gr:stream-id", node);
            log.info("Found rss stream:" + stream);
            if (streamsToMonitor.contains(stream)) {
                String title = xpath.evaluate("ns:title", node);
                if (containsSubjectsToIgnore(title)) {
                    continue;
                }
                String id = xpath.evaluate("ns:id", node);
                String url = xpath.evaluate("ns:link[@rel='alternate']/@href", node);
                itemTagToURL.put(id, url);
            }
        }
        return itemTagToURL;
    }

    private boolean containsSubjectsToIgnore(String title) {
        String titleLowerCase = title.toLowerCase();
        for (String topicToIgnore : SUBJECT_WORDS_TO_IGNORE) {
            if (titleLowerCase.contains(topicToIgnore)) {
                log.info("Ignoring title: " + title);
                return true;
            }
        }
        return false;
    }

    private class DefaultNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if (prefix == null) throw new NullPointerException("Null prefix");
            else if ("ns".equals(prefix)) return "http://www.w3.org/2005/Atom";
            else if ("gr".equals(prefix)) return "http://www.google.com/schemas/reader/atom/";
            else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
            return XMLConstants.NULL_NS_URI;
        }

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }

    }
}
