package servlets;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static services.HttpUtils.renderHtml;


/**
 *
 */
@Component
public class MyAdminServlet extends HttpServlet
{
    private final OAuthContext oAuthContext;

    @Autowired
    public MyAdminServlet(OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = oAuthContext.validate2LOFromParameters(req);
        final Map<String, Object> context = new HashMap<String,Object>();
        context.put("consumerKey", consumerKey);
        context.put("baseUrl", oAuthContext.getHostBaseUrl(consumerKey));
        renderHtml(resp, "test-page.mu", context);
    }

}
