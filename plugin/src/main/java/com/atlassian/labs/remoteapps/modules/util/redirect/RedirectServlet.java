package com.atlassian.labs.remoteapps.modules.util.redirect;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.EncodingUtils.urlEncode;
import static com.google.common.collect.Maps.newHashMap;

/**
 * <p>
 * Given an app key ID and a relative path, builds an absolute URL to the Remote Application,
 * optionally signs the URL using OAuth and then redirects the client to the signed URL.
 * </p><p>
 * Used in conjunction with {@link com.atlassian.labs.remoteapps.modules.confluence.MacroContentLinkParser}, provides a
 * way for remote macros to provide authenticated links back in to the remote application.
 * </p>
 *
 * @see {@link com.atlassian.labs.remoteapps.modules.confluence.MacroContentLinkParser}
 */
public class RedirectServlet extends HttpServlet
{
    private static final String APP_KEY_PARAM = "app_key";
    private static final String APP_URL_PARAM = "app_url";

    private final ApplicationLinkOperationsFactory appLinkOperationsFactory;
    private final UserManager userManager;
    private final ApplicationLinkAccessor applicationLinkAccessor;

    public RedirectServlet(UserManager userManager,
            ApplicationLinkOperationsFactory appLinkOperationsFactory,
            ApplicationLinkAccessor applicationLinkAccessor)
    {
        this.userManager = userManager;
        this.appLinkOperationsFactory = appLinkOperationsFactory;
        this.applicationLinkAccessor = applicationLinkAccessor;
    }

    /**
     * @return a relative url that doesn't include the context path
     */
    public static String getPermanentRedirectUrl(String appKey, URI path)
    {
        return String.format("/plugins/servlet/redirect/permanent?%s=%s&%s=%s",
                RedirectServlet.APP_KEY_PARAM, urlEncode(appKey),
                RedirectServlet.APP_URL_PARAM, urlEncode(path.toString()));
    }

    /**
     * @return an absolute url that includes oauth signing information as query parameters
     */
    public static String getOAuthRedirectUrl(String baseUrl, String appKey, URI path)
    {
        return String.format("%s/plugins/servlet/redirect/oauth?%s=%s&%s=%s",
                baseUrl,
                RedirectServlet.APP_KEY_PARAM, urlEncode(appKey),
                RedirectServlet.APP_URL_PARAM, urlEncode(path.toString()));
    }
    /**
     * Expected URL query parameters:
     * <ul>
     * <li><strong>app_key:</strong> The Remote App key</li>
     * <li><strong>app_url:</strong> Relative URL within the Remote App to redirect to</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        RedirectType redirectType = RedirectType.fromRequest(req);

        final String appkey = req.getParameter(APP_KEY_PARAM);
        Validate.notEmpty(appkey, String.format("%s parameter is required", APP_KEY_PARAM));

        final String appUrl = req.getParameter(APP_URL_PARAM);
        Validate.notEmpty(appUrl, String.format("%s parameter is required", APP_URL_PARAM));

        ApplicationLink applicationLink;
        try
        {
            applicationLink = applicationLinkAccessor.getApplicationLink(appkey);
        }
        catch (IllegalArgumentException ex)
        {
            sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, String.format("Remote App %s does not exist", appkey));
            return;
        }

        String fullAppUrl = redirectType.isSign() ? getFullSignedUrl(applicationLink, appUrl, req.getParameterMap()) :
                getFullUrl(applicationLink, appUrl, req.getParameterMap());

        resp.setStatus(redirectType.getStatusCode());
        resp.addHeader(HttpHeaders.LOCATION, fullAppUrl);
        resp.getOutputStream().close();
    }

    private String getFullUrl(ApplicationLink appLink, String appRelativeUrl, Map<String,String[]> params)
    {
        Uri targetUrl = Uri.parse(appRelativeUrl);
        ApplicationLinkOperationsFactory.LinkOperations linkOps = appLinkOperationsFactory.create(
                appLink.getType());
        return linkOps.createGetUrl(targetUrl.getPath(), params);
    }

    private String getFullSignedUrl(ApplicationLink appLink, String appRelativeUrl,
            Map<String,String[]> parameterMap)
    {
        // Build & Sign the URL
        Uri targetUrl = Uri.parse(appRelativeUrl);

        ApplicationLinkOperationsFactory.LinkOperations linkOps = appLinkOperationsFactory.create(appLink.getType());
        Map<String,String[]> params = newHashMap(parameterMap);
        params.putAll(Maps.transformValues(targetUrl.getQueryParameters(),
                new Function<List<String>, String[]>()
                {
                    @Override
                    public String[] apply(List<String> strings)
                    {
                        return strings.toArray(new String[strings.size()]);
                    }
                }));
        return linkOps.signGetUrl(userManager.getRemoteUsername(), targetUrl.getPath(), params);
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
            {
                writer.close();
            }
        }
    }

    private static enum RedirectType
    {
        OAUTH_TEMPORARY("oauth", true, HttpStatus.SC_MOVED_TEMPORARILY),
        PERMANENT("permanent", false, HttpStatus.SC_MOVED_PERMANENTLY);

        private final String type;
        private final boolean sign;
        private final int statusCode;

        private RedirectType(String type, boolean sign, int statusCode)
        {
            this.type = type;
            this.sign = sign;
            this.statusCode = statusCode;
        }

        public boolean isSign()
        {
            return sign;
        }

        public int getStatusCode()
        {
            return statusCode;
        }

        public static RedirectType fromRequest(HttpServletRequest req)
        {
            String requestUri = req.getRequestURI();
            if (requestUri != null)
            {
                String typeId = requestUri.substring(requestUri.lastIndexOf("/") + 1);
                for (RedirectType redirectType : values())
                {
                    if (redirectType.type.equals(typeId))
                    {
                        return redirectType;
                    }
                }
                throw new IllegalArgumentException("Redirect type not found: " + typeId);
            }
            throw new IllegalArgumentException("Redirect type not found");
        }
    }
}
