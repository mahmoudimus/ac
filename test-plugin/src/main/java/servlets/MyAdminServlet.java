package servlets;

import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.remotable.api.annotation.ServiceReference;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.kit.servlet.AbstractPageServlet;
import com.atlassian.plugin.remotable.kit.servlet.AppScripts;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
@AppScripts({"jquery-1.7", "my-admin"})
public class MyAdminServlet extends AbstractPageServlet
{
    @ServiceReference
    private HostHttpClient httpClient;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        Response response = httpClient.newRequest("/rest/remoteplugintest/1/user").get().claim();
        render(req, res, ImmutableMap.of(
            "httpGetStatus", response.getStatusCode(),
            "httpGetStatusText", response.getStatusText(),
            "httpGetContentType", response.getContentType(),
            "httpGetEntity", response.getEntity()
        ));
    }
}
