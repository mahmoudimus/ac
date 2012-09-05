package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.plugin.util.PluginUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.IOException;
import java.io.PrintWriter;

public class MultipageServlet extends HttpServlet
{
    private String internalUrl;
    private String hostBaseUrl;
    private final RequestContext requestContext;

    public MultipageServlet(String internalUrl, String hostBaseUrl, RequestContext requestContext)
    {
        this.internalUrl = internalUrl;
        this.hostBaseUrl = hostBaseUrl;
        this.requestContext = requestContext;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        boolean isDevMode = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
        res.setStatus(200);
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.print("<!DOCTYPE html>");
        out.print("<html>");
        out.print("<head>");
        out.print("<meta charset='utf-8'>");
        out.print("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>");
        out.print("<style>html, body, iframe {padding:0;margin:0;background:transparent;border: none;}</style>");
        out.print("<script src='" + hostBaseUrl + "/remoteapps/all"  + (isDevMode ? "-debug" : "") + ".js'></script>");
        out.print("<script>RA.init('bridge');</script>");
        out.print("</head>");
        out.print("<body>");
        out.print("<iframe src='" + getSignedInternalUrl() + "'></iframe>");
        out.print("</body>");
        out.print("</html>");
        out.flush();
    }

    private String getSignedInternalUrl()
    {
        String clientKey = requestContext.getClientKey();
        // @todo request signing with client key
        return internalUrl;
    }
}
