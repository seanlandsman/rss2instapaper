package com.seanlandsman.camera;

import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityCamServlet extends HttpServlet {
    private static Key securityCamAppDataKey = KeyFactory.createKey("AppData", "SecurityCamera");
    private static final String REQUEST_TYPE = "REQUEST_TYPE";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (securityCheckPresent(request)) {
            String requestType = request.getParameter(REQUEST_TYPE);
            if ("GET_SETTINGS".equals(requestType)) {
                response.getWriter().println("Settings: " + shouldTurnCameraAlarmOff());
            } else if ("REQUEST_ALARM_BE_TURNED_OFF".equals(requestType)) {
                turnCameraOff(true);
            } else if ("RESET_SETTINGS".equals(requestType)) {
                turnCameraOff(false);
            }
        }
    }

    private boolean securityCheckPresent(HttpServletRequest request) {
        return "Fandoogle".equalsIgnoreCase(request.getParameter("CHECK"));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private boolean shouldTurnCameraAlarmOff() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity invoiceAppData = datastore.get(securityCamAppDataKey);
            return (Boolean) invoiceAppData.getProperty("turnCameraAlarmOff");
        } catch (EntityNotFoundException ignored) {
        }
        return false;
    }

    private void turnCameraOff(boolean turnCameraAlarmOff) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity cameraDataKey = new Entity(securityCamAppDataKey);
        cameraDataKey.setProperty("turnCameraAlarmOff", turnCameraAlarmOff);
        datastore.put(cameraDataKey);
    }
}
