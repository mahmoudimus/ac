package com.atlassian.labs.remoteapps.sample;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 */
public class MacroResetServlet extends HttpServlet
{
    private final String appKey;
    private final OAuthContext oAuthContext;

    public MacroResetServlet(String appKey, OAuthContext oAuthContext)
    {
        this.appKey = appKey;
        this.oAuthContext = oAuthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        for (String clientKey : Environment.getAllClients())
        {
            URL url = new URL(oAuthContext.getHostBaseUrl(clientKey) + "/rest/remoteapps/latest/macro/app/" + appKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            oAuthContext.sign(url.toString(), "DELETE", null, conn);
            int code = conn.getResponseCode();
            System.out.println("Reset from " + clientKey + " returned: " + code);
            resp.setStatus(code);
            conn.disconnect();
        }
    }
}
