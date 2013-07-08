package com.seanlandsman.reader2paper.servlet;

import com.seanlandsman.reader2paper.RssToInstapaperService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class RssServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(RssServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        log.info("Attempting to process rss feed");

        RssToInstapaperService rssToInstapaperService;
        try {
            rssToInstapaperService = new RssToInstapaperService();
        } catch (Exception e) {
            log.severe("Could not create RssToInstapaperService" + e.getMessage());
            throw new ServletException(e);
        }

        try {
            rssToInstapaperService.processRssFeedsAndSendToInstapaper();
        } catch (Exception e) {
            log.severe("Could not refresh from rss feed:" + e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
