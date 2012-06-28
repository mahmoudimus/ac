package servlets;

import com.atlassian.labs.remoteapps.apputils.spring.EnvironmentImpl;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class MacroResetServlet extends HttpServlet
{
    private final OAuthContext oAuthContext;

    @Autowired
    public MacroResetServlet(OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
    }   

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String baseUrl = req.getParameter("baseurl");
        URL url = new URL(baseUrl + "/rest/remoteapps/latest/macro/app/app1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        oAuthContext.sign(url.toString(), "DELETE", null, conn);
        int code = conn.getResponseCode();
        System.out.println("Reset from " + baseUrl + " returned: " + code);
        resp.setStatus(code);
        conn.disconnect();
    }
}
