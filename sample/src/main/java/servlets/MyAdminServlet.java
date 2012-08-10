package servlets;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.http.Response;
import com.atlassian.labs.remoteapps.api.services.http.SyncHostHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static services.HttpUtils.renderHtml;

@Singleton
public class MyAdminServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;
    private SyncHostHttpClient hostHttpClient;

    @Inject
    public MyAdminServlet(@ServiceReference SignedRequestHandler signedRequestHandler,
                          @ServiceReference SyncHostHttpClient hostHttpClient)
    {
        this.signedRequestHandler = signedRequestHandler;
        this.hostHttpClient = hostHttpClient;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        String consumerKey = signedRequestHandler.validateRequest(req);
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("consumerKey", consumerKey);
        context.put("baseUrl", signedRequestHandler.getHostBaseUrl(consumerKey));
        execHostHttpRequests(context);
        renderHtml(resp, "test-page.mu", context);
    }

    private void execHostHttpRequests(Map<String, Object> context)
        throws ServletException, IOException
    {
        Response response = hostHttpClient.get("/rest/remoteapptest/1/user");
        context.put("httpGetStatus", response.getStatusCode());
        context.put("httpGetStatusText", response.getStatusText());
        context.put("httpGetContentType", response.getContentType());
        context.put("httpGetEntity", response.getEntity());
    }

}
