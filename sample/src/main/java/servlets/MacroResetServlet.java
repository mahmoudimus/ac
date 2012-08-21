package servlets;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
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
@Singleton
public class MacroResetServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Inject
    public MacroResetServlet(@ServiceReference SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }   

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String baseUrl = req.getParameter("baseurl");
        URL url = new URL(baseUrl + "/rest/remoteapps/latest/macro/app/app1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        signedRequestHandler.sign(url.toString(), "DELETE", null, conn);
        int code = conn.getResponseCode();
        System.out.println("Reset from " + baseUrl + " returned: " + code);
        resp.setStatus(code);
        conn.disconnect();
    }
}
