package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
@Named
public class MacroResetServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Inject
    public MacroResetServlet(@ComponentImport SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }   

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String baseUrl = req.getParameter("baseurl");
        URL url = new URL(baseUrl + "/rest/remotable-plugins/latest/macro/app/app1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        try
        {
            signedRequestHandler.sign(url.toURI(), "DELETE", null, conn);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        int code = conn.getResponseCode();
        System.out.println("Reset from " + baseUrl + " returned: " + code);
        resp.setStatus(code);
        conn.disconnect();
    }
}
