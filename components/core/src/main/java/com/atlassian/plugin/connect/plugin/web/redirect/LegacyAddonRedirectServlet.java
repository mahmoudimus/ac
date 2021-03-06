package com.atlassian.plugin.connect.plugin.web.redirect;

import com.atlassian.plugin.connect.api.request.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.uri.Uri;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Map;

/**
 * Given an addon key and a relative path, builds an absolute URL and redirects the client to it.
 * @deprecated do not use redirects - use the direct add-on url instead
 */
@Deprecated
public final class LegacyAddonRedirectServlet extends HttpServlet {
    private static final String APP_KEY_PARAM = "app_key";
    private static final String APP_URL_PARAM = "app_url";

    private static final Logger log = LoggerFactory.getLogger(LegacyAddonRedirectServlet.class);

    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;

    public LegacyAddonRedirectServlet(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory) {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    /**
     * Expected URL query parameters:
     * <ul>
     *     <li><strong>app_key:</strong> The Remotable Plugin key</li>
     *     <li><strong>app_url:</strong> Relative URL within the Remotable Plugin to redirect to</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.warn("/plugins/servlet/redirect/permanent resource is deprecated and will be removed soon. Please use a direct url to the add-on.");
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

    private String getFullUrl(RemotablePluginAccessor remotablePluginAccessor, String appRelativeUrl, Map<String, String[]> params) {
        Uri targetUrl = Uri.parse(appRelativeUrl);
        return remotablePluginAccessor.createGetUrl(targetUrl.toJavaUri(), params);
    }
}
