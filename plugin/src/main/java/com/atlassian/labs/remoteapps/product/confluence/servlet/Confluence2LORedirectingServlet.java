package com.atlassian.labs.remoteapps.product.confluence.servlet;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * <p>
 * Given an {@link ApplicationLink} ID and a relative URL, builds an absolute URL to the Remote Application, signs the
 * URL using OAuth and then redirects the client to the signed URL.
 * </p><p>
 * Used in conjunction with {@link com.atlassian.labs.remoteapps.modules.confluence.MacroContentLinkParser}, provides a
 * way for remote macros to provide authenticated links back in to the remote application.
 * </p>
 *
 * @see {@link com.atlassian.labs.remoteapps.modules.confluence.MacroContentLinkParser}
 */
// TODO: This servlet could be moved out of the Confluence-specific plugin and into the wider Remote App plugin if the need arises
public class Confluence2LORedirectingServlet extends HttpServlet
{
    private static final String APP_LINK_ID_PARAM = "app_link_id";
    private static final String APP_URL_PARAM = "app_url";

    private final ApplicationLinkService applicationLinkService;
    private final ApplicationLinkOperationsFactory appLinkOperationsFactory;
    private final UserManager userManager;
    private final PermissionManager permissionManager;

    public Confluence2LORedirectingServlet(ApplicationLinkService applicationLinkService, UserManager userManager, ApplicationLinkOperationsFactory appLinkOperationsFactory, PermissionManager permissionManager)
    {
        this.applicationLinkService = applicationLinkService;
        this.userManager = userManager;
        this.appLinkOperationsFactory = appLinkOperationsFactory;
        this.permissionManager = permissionManager;
    }

    /**
     * Expected URL query parameters:
     * <ul>
     * <li><strong>app_link_id:</strong> Unique ID of the primary Application Link for the desired Remote App</li>
     * <li><strong>app_url:</strong> Relative URL within the Remote App to redirect to</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String appLinkId = req.getParameter(APP_LINK_ID_PARAM);
        Validate.notEmpty(appLinkId, String.format("%s parameter is required", APP_LINK_ID_PARAM));

        final String appUrl = req.getParameter(APP_URL_PARAM);
        Validate.notEmpty(appUrl, String.format("%s parameter is required", APP_URL_PARAM));

        final ApplicationLink applicationLink;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(new ApplicationId(appLinkId)); // IllegalArgumentException in AppId constructor
            Validate.notNull(applicationLink, String.format("Application Link %s does not exist", appLinkId));
        }
        catch (IllegalArgumentException e) // Thrown by the ApplicationId constructor if the ID string is not in the right format.
        {
            sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, String.format("Application Link %s does not exist", appLinkId));
            return;
        }
        catch (TypeNotInstalledException e)
        {
            sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, String.format("Application Link %s parent type was not installed", appLinkId));
            return;
        }

        // Does the current user have permission to use the desired Remote App? (this is actually also checked in linkOps#signGetUrl,
        // but it doesn't hurt to be cautious.
        if (!permissionManager.canCurrentUserAccessRemoteApp(req, applicationLink))
        {
            sendResponse(resp, HttpServletResponse.SC_FORBIDDEN, "You cannot access that Remote Application.");
            return;
        }

        resp.sendRedirect(getFullSignedUrl(applicationLink, appUrl));
    }

    private String getFullSignedUrl(ApplicationLink appLink, String appRelativeUrl)
    {
        // Build & Sign the URL
        Uri targetUrl = Uri.parse(appRelativeUrl);

        ApplicationLinkOperationsFactory.LinkOperations linkOps = appLinkOperationsFactory.create(appLink.getType());
        return linkOps.signGetUrl(userManager.getRemoteUsername(), targetUrl.getPath(), Maps.transformValues(targetUrl.getQueryParameters(), new Function<List<String>, String[]>()
        {
            @Override
            public String[] apply(List<String> strings)
            {
                return strings.toArray(new String[strings.size()]);
            }
        }));
    }

    private void sendResponse(HttpServletResponse response, int statusCode, String statusMessage) throws IOException
    {
        response.setStatus(statusCode);
        response.setContentType("text/plain");

        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            writer.write(statusMessage);
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
    }
}
