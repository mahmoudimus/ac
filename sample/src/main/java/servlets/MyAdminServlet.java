package servlets;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.Response;

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
    private HostHttpClient httpClient;

    @Inject
    public MyAdminServlet(@ServiceReference SignedRequestHandler signedRequestHandler,
                          @ServiceReference HostHttpClient httpClient)
    {
        this.signedRequestHandler = signedRequestHandler;
        this.httpClient = httpClient;
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
        Response response = httpClient.newRequest("/rest/remoteapptest/1/user").get().claim();
        context.put("httpGetStatus", response.getStatusCode());
        context.put("httpGetStatusText", response.getStatusText());
        context.put("httpGetContentType", response.getContentType());
        context.put("httpGetEntity", response.getEntity());
    }

}
