package com.atlassian.plugin.connect.plugin.module.util.redirect;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import static com.google.common.collect.Maps.newHashMap;

/**
 * <p>
 * Given an app key ID and a relative path, builds an absolute URL to the Remotable Plugin,
 * optionally signs the URL using OAuth and then redirects the client to the signed URL.
 * </p><p>
 * Used in conjunction with {@link com.atlassian.plugin.connect.plugin.module.confluence.MacroContentLinkParser}, provides a
 * way for remote macros to provide authenticated links back in to the remote plugin.
 * </p>
 *
 * @see {@link com.atlassian.plugin.connect.plugin.module.confluence.MacroContentLinkParser}
 */
public final class RedirectServlet extends HttpServlet
{
    private static final String APP_KEY_PARAM = "app_key";
    private static final String APP_URL_PARAM = "app_url";

    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final UserManager userManager;

    public RedirectServlet(UserManager userManager,
                           DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory
    )
    {
        this.userManager = userManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
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
     * <li><strong>app_key:</strong> The Remotable Plugin key</li>
     * <li><strong>app_url:</strong> Relative URL within the Remotable Plugin to redirect to</li>
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

        final RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(appkey);

        String fullAppUrl = redirectType.isSign() ? getFullSignedUrl(remotablePluginAccessor, appUrl, req.getParameterMap()) :
                getFullUrl(remotablePluginAccessor, appUrl, req.getParameterMap());

        resp.setStatus(redirectType.getStatusCode());
        resp.addHeader(HttpHeaders.LOCATION, fullAppUrl);
        resp.getOutputStream().close();
    }

    private String getFullUrl(RemotablePluginAccessor remotablePluginAccessor, String appRelativeUrl, Map<String,String[]> params)
    {
        Uri targetUrl = Uri.parse(appRelativeUrl);
        return remotablePluginAccessor.createGetUrl(targetUrl.toJavaUri(), params);
    }

    private String getFullSignedUrl(RemotablePluginAccessor remotablePluginAccessor, String appRelativeUrl,
            Map<String,String[]> parameterMap)
    {
        // Build & Sign the URL
        Uri targetUrl = Uri.parse(appRelativeUrl);

        Map<String,String[]> params = newHashMap(parameterMap);
        
        /*
         TODO: UserManager is flawed in that it will return a UserProfile instead of null even when the underlying user is null.
         To get around this, we need to use the deprecated getRemoteUsername and check that until the products adopt the fixed sal
         */
        UserProfile remoteUser = userManager.getRemoteUser();
        String remoteUsername = userManager.getRemoteUsername();
        if (remoteUser != null && StringUtils.isNotBlank(remoteUsername)) {
            params.put("user_id", new String[]{ remoteUsername });
            params.put("user_key", new String[]{ remoteUser.getUserKey().getStringValue() });
        }
        params.putAll(Maps.transformValues(targetUrl.getQueryParameters(),
                new Function<List<String>, String[]>()
                {
                    @Override
                    public String[] apply(List<String> strings)
                    {
                        return strings.toArray(new String[strings.size()]);
                    }
                }));
        return remotablePluginAccessor.signGetUrl(targetUrl.toJavaUri(), params);
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
