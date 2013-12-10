package com.atlassian.plugin.connect.plugin.module.util.redirect;

import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

/**
 * Given an addon key and a relative path, builds an absolute URL and redirects the client to it.
 */
public final class RedirectServlet extends HttpServlet
{
    private static final String APP_KEY_PARAM = "app_key";
    private static final String APP_URL_PARAM = "app_url";

    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;

    public RedirectServlet(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
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
     * Expected URL query parameters:
     * <ul>
     *     <li><strong>app_key:</strong> The Remotable Plugin key</li>
     *     <li><strong>app_url:</strong> Relative URL within the Remotable Plugin to redirect to</li>
     * </ul>
     */
    @SuppressWarnings ("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String appkey = req.getParameter(APP_KEY_PARAM);
        Validate.notEmpty(appkey, String.format("%s parameter is required", APP_KEY_PARAM));

        final String appUrl = req.getParameter(APP_URL_PARAM);
        Validate.notEmpty(appUrl, String.format("%s parameter is required", APP_URL_PARAM));

        final RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(appkey);

        Map<String, String[]> parameterMap = (Map<String, String[]>) req.getParameterMap();
        String fullAppUrl = getFullUrl(remotablePluginAccessor, appUrl, parameterMap);

        resp.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
        resp.addHeader(HttpHeaders.LOCATION, fullAppUrl);
        resp.getOutputStream().close();
    }

    private String getFullUrl(RemotablePluginAccessor remotablePluginAccessor, String appRelativeUrl, Map<String, String[]> params)
    {
        Uri targetUrl = Uri.parse(appRelativeUrl);
        return remotablePluginAccessor.createGetUrl(targetUrl.toJavaUri(), params);
    }
}
