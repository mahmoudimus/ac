package com.atlassian.plugin.remotable.plugin.loader.universalbinary;

import com.atlassian.plugin.Plugin;
import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Simply serves up resources with no caching header support.
 */
public class StaticResourceServlet extends HttpServlet
{
    private final Plugin plugin;
    private final String resourceBase;

    public StaticResourceServlet(Plugin plugin, String resourceBase)
    {
        this.plugin = plugin;
        this.resourceBase = resourceBase;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        String path = req.getPathInfo();
        URL url = findResourceUrl(path);
        if (url == null)
        {
            send404(res);
            return;
        }

        res.setHeader("Vary", "Accept-Encoding");
        res.setContentType(getServletContext().getMimeType(path));
        res.setHeader("Connection", "keep-alive");

        InputStream in = null;
        try
        {
            byte[] localData = IOUtils.toByteArray(url.openStream());
            res.setContentLength(localData.length);
            ServletOutputStream sos = res.getOutputStream();
            sos.write(localData);
            sos.close();
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    private URL findResourceUrl(String path)
    {
        return plugin.getResource(resourceBase + path);
    }

    private void send404(HttpServletResponse res) throws IOException
    {
        res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find resource");
    }

    @Override
    public void destroy()
    {
    }
}
