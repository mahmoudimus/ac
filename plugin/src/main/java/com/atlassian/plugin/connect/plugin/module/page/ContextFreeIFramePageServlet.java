package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.module.IFramePageRenderer;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A servlet that loads its content from a remote plugin's iframe.
 * @deprecated this is insecure. will be deleted in an upcoming release.
 * TODO: Remove this class when support for XML Descriptors goes away
 */
@Deprecated
@XmlDescriptor
public class ContextFreeIFramePageServlet extends HttpServlet
{
    private final UserManager userManager;
    private final IFramePageRenderer iFramePageRenderer;
    private final  JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;

    public ContextFreeIFramePageServlet(IFramePageRenderer iFramePageRenderer,
                                        UserManager userManager, JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService)
    {
        this.iFramePageRenderer = iFramePageRenderer;
        this.userManager = userManager;
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                                                                                  IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");

        String[] pluginKeys = req.getParameterValues("plugin-key");
        String[] remoteUrls = req.getParameterValues("remote-url");

        if (null == pluginKeys || pluginKeys.length == 0 || StringUtils.isBlank(pluginKeys[0]))
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "plugin-key is required");
            return;
        }

        if (null == remoteUrls || remoteUrls.length == 0 || StringUtils.isBlank(remoteUrls[0]))
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "remote-url is required");
            return;
        }

        String pluginKey = pluginKeys[0];
        String remoteUrl = remoteUrls[0];

        if (jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey))
        {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Getting the iFrame content for arbitrary URLs is no longer supported. Use 'getIframeHtmlForKey' instead of 'getIframeHtmlForUrl'.");
            return;
        }

        boolean dialog = req.getParameterValues("dialog") != null && req.getParameterValues("dialog").length > 0;

        String namespace = pluginKey + (dialog ? "-dialog" : ""); // iframe id will be <pluginKey>-dialog
        String templateSuffix = dialog ? "-dialog" : "";

        IFrameParams iFrameParams = new ContextFreeIFrameParamsImpl();
        IFrameContext iframeContext = new IFrameContextImpl(pluginKey, remoteUrl, namespace, iFrameParams);
        PageInfo pageInfo = new PageInfo("", templateSuffix, "", new AlwaysDisplayCondition(), Collections.<String, String> emptyMap());
        if (dialog)
        {
            iFrameParams.setParam("dialog", "1");
        }

        iFramePageRenderer.renderPage(iframeContext, pageInfo, req.getPathInfo(), req.getParameterMap(),
                userManager.getRemoteUsername(req), Collections.<String, Object>emptyMap(), out);

    }

    private class ContextFreeIFrameParamsImpl implements IFrameParams
    {
        private final Map<String, Object> params;

        public ContextFreeIFrameParamsImpl()
        {
            params = newHashMap();
        }

        @Override
        public Map<String, Object> getAsMap()
        {
            return params;
        }

        @Override
        public void setParam(String key, String value)
        {
            params.put(key, value);
        }
    }
}
