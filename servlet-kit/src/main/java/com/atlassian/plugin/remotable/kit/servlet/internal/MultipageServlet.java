package com.atlassian.plugin.remotable.kit.servlet.internal;

import com.atlassian.plugin.util.PluginUtils;
import com.google.common.base.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MultipageServlet extends HttpServlet
{
    private Supplier<String> internalUrl;
    private Supplier<String> hostBaseUrl;

    public MultipageServlet(Supplier<String> internalUrl, Supplier<String> hostBaseUrl)
    {
        this.internalUrl = checkNotNull(internalUrl);
        this.hostBaseUrl = checkNotNull(hostBaseUrl);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.print("<!DOCTYPE html>");
        out.print("<html>");
        out.print("<head>");
        out.print("<meta charset='utf-8'>");
        out.print("<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'>");
        out.print("<style>html, body, iframe {padding:0;margin:0;background:transparent;border: none;}</style>");
        out.printf("<script src='%s' data-bridge='true'></script>", getAllJsSrc());
        out.print("</head>");
        out.print("<body>");
        out.printf("<iframe src='%s'></iframe>", getIFrameSrc());
        out.print("</body>");
        out.print("</html>");
        out.flush();
    }

    private String getAllJsSrc()
    {
        return String.format("%s/remotable-plugins/all%s.js", hostBaseUrl.get(), isDevMode() ? "-debug" : "");
    }

    private String getIFrameSrc()
    {
        return internalUrl.get();
    }

    private static boolean isDevMode()
    {
        return Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
    }
}
