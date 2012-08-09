package com.atlassian.labs.remoteapps.modules.util.redirect;

import com.atlassian.labs.remoteapps.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.labs.remoteapps.util.uri.UriBuilder;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private final RemoteAppAccessorFactory remoteAppAccessorFactory;
    private final UserManager userManager;

    public RedirectServlet(UserManager userManager,
            RemoteAppAccessorFactory remoteAppAccessorFactory)
    {
        this.userManager = userManager;
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;
    }

    /**
     * @return a relative url that doesn't include the context path
     */
    public static String getPermanentRedirectUrl(String appKey, URI path)
    {
        return new UriBuilder(Uri.parse("/plugins/servlet/redirect/permanent"))
                .addQueryParameter(RedirectServlet.APP_KEY_PARAM, appKey)
                .addQueryParameter(RedirectServlet.APP_URL_PARAM, path.toString())
                .toString();
    }

    /**
     * @return an absolute url that includes oauth signing information as query parameters
     */
    public static String getOAuthRedirectUrl(String baseUrl, String appKey, URI path)
    {
       return getOAuthRedirectUrl(baseUrl, appKey, path, Collections.<String, String>emptyMap());
    }

    /**
     * @return an absolute url that includes oauth signing information as query parameters
     */
    public static String getRelativeOAuthRedirectUrl(String appKey, URI path, Map<String,String> params)
    {
        return getOAuthRedirectUrl("", appKey, path, params);
    }
    /**
     * @return an absolute url that includes oauth signing information as query parameters
     */
    public static String getOAuthRedirectUrl(String baseUrl, String appKey, URI path, Map<String,String> params)
    {
        UriBuilder builder = new UriBuilder(Uri.parse(baseUrl + "/plugins/servlet/redirect/oauth"))
                .addQueryParameter(RedirectServlet.APP_KEY_PARAM, appKey)
                .addQueryParameter(RedirectServlet.APP_URL_PARAM, path.toString());

        for (Map.Entry<String,String> entry : params.entrySet())
        {
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        return builder.toString();
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

        RemoteAppAccessor remoteAppAccessor = remoteAppAccessorFactory.get(appkey);

        String fullAppUrl = redirectType.isSign() ? getFullSignedUrl(remoteAppAccessor, appUrl, req.getParameterMap()) :
                getFullUrl(remoteAppAccessor, appUrl, req.getParameterMap());

        resp.setStatus(redirectType.getStatusCode());
        resp.addHeader(HttpHeaders.LOCATION, fullAppUrl);
        resp.getOutputStream().close();
    }

    private String getFullUrl(RemoteAppAccessor remoteAppAccessor, String appRelativeUrl, Map<String,String[]> params)
    {
        Uri targetUrl = Uri.parse(appRelativeUrl);
        return remoteAppAccessor.createGetUrl(targetUrl.getPath(), params);
    }

    private String getFullSignedUrl(RemoteAppAccessor remoteAppAccessor, String appRelativeUrl,
            Map<String,String[]> parameterMap)
    {
        // Build & Sign the URL
        Uri targetUrl = Uri.parse(appRelativeUrl);

        Map<String,String[]> params = newHashMap(parameterMap);
        params.put("user_id", new String[]{userManager.getRemoteUsername()});
        params.putAll(Maps.transformValues(targetUrl.getQueryParameters(),
                new Function<List<String>, String[]>()
                {
                    @Override
                    public String[] apply(List<String> strings)
                    {
                        return strings.toArray(new String[strings.size()]);
                    }
                }));
        return remoteAppAccessor.signGetUrl(targetUrl.getPath(), params);
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
