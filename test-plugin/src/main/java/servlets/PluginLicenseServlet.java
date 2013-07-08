package servlets;

import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static services.HttpUtils.renderHtml;

@Named
public class PluginLicenseServlet extends HttpServlet
{
    @Inject
    @ComponentImport
    private HostHttpClient httpClient;

    @Inject
    @ComponentImport
    private RequestContext requestContext;

    public PluginLicenseServlet()
    {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        Response licenseResponse = httpClient.newRequest("/rest/remotable-plugins/latest/license/").get().claim();
        renderHtml(resp, "plugin-license.mu", ImmutableMap.<String, Object>builder()
                .put("licenseResponseStatusCode", licenseResponse.getStatusCode())
                .put("baseUrl", requestContext.getHostBaseUrl())
                .build()
        );
    }
}
