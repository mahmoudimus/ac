package com.atlassian.plugin.connect.plugin.module.page;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.plugin.module.IFramePageRenderer;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.Maps;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A servlet that loads its content from a remote plugin's iframe.
 * @deprecated this is insecure. will be deleted in an upcoming release.
 */
@Deprecated
public class ContextFreeIFramePageServlet extends HttpServlet
{
    private final UserManager userManager;
    private final IFramePageRenderer iFramePageRenderer;

    public ContextFreeIFramePageServlet(IFramePageRenderer iFramePageRenderer,
                                        UserManager userManager)
    {
        this.iFramePageRenderer = iFramePageRenderer;
        this.userManager = userManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                                                                                  IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");

        String pluginKey = req.getParameterValues("plugin-key")[0];
        String remoteUrl = req.getParameterValues("remote-url")[0];
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
