package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.license.RemotablePluginLicense;
import com.atlassian.plugin.remotable.api.service.license.RemotablePluginLicenseRetriever;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
@Named
public class LicenseRetrieverServlet extends HttpServlet
{
    @Inject
    @ComponentImport
    RemotablePluginLicenseRetriever remotableLicenseRetriever;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/plain");
        RemotablePluginLicense pluginLicense = remotableLicenseRetriever.retrieve().claim();
        resp.getWriter().write(pluginLicense != null ? pluginLicense.getLicenseType() : "");
        resp.getWriter().close();
    }
}
