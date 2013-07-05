package servlets;

import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.pluginkit.servlet.AbstractPageServlet;
import com.atlassian.pluginkit.servlet.AppScripts;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Named
@AppScripts ({ "jquery-1.7", "my-admin" })
public class MyAdminServlet extends AbstractPageServlet
{
    @Inject
    @ComponentImport
    private HostHttpClient httpClient;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException
    {
        Response response = httpClient.newRequest("/rest/remoteplugintest/1/user").get().claim();
        Response licenseResponse = httpClient.newRequest("/rest/remotable-plugins/latest/license/").get().claim();
        render(req, res, ImmutableMap.<String, Object>builder()
                .put("httpGetStatus", response.getStatusCode())
                .put("httpGetStatusText", response.getStatusText())
                .put("httpGetContentType", response.getContentType())
                .put("httpGetEntity", response.getEntity())
                .put("licenseStatus", req.getParameter("lic"))
                .put("timeZone", req.getParameter("tz"))
                .put("locale", req.getParameter("loc"))
                .put("licenseResponseStatusCode", licenseResponse.getStatusCode())
                .build());
    }
}
