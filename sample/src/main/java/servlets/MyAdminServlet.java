package servlets;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
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
    private final SignedRequestHandler signedRequestHandler;

    @Autowired
    public MyAdminServlet(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = signedRequestHandler.validateRequest(req);
        final Map<String, Object> context = new HashMap<String,Object>();
        context.put("consumerKey", consumerKey);
        context.put("baseUrl", signedRequestHandler.getHostBaseUrl(consumerKey));
        renderHtml(resp, "test-page.mu", context);
    }

}
