package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.util.BundleUtil;
import com.atlassian.labs.remoteapps.util.RemoteAppManifestReader;
import com.atlassian.plugin.util.PluginUtils;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serves remote app files when installed from a local file url.  Only allowed in dev mode.
 */
public class FileRemoteAppFilter implements Filter
{
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("/([-a-zA-Z0-9._]+)/([a-zA-Z0-9-_/]+\\.(?:js|css|html))");
    private final BundleContext bundleContext;

    public FileRemoteAppFilter(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
            IOException, ServletException
    {
        if (!Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE))
        {
            ((HttpServletResponse)response).sendError(403, "Only allowed in dev mode");
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length() + "/app".length());
        Matcher m = RESOURCE_PATTERN.matcher(path);
        if (m.matches())
        {
            String appKey = m.group(1);
            String localPath = m.group(2);

            Bundle appBundle = BundleUtil.findBundleForPlugin(bundleContext, appKey);
            File descriptorFile = new File(
                    URI.create(RemoteAppManifestReader.getRegistrationUrl(appBundle)));
            File baseFile = descriptorFile.getParentFile();

            File localFile = new File(baseFile, localPath);
            if (!localFile.exists())
            {
                send404(res);
                return;
            }
            byte[] localData = FileUtils.readFileToByteArray(localFile);

            res.setHeader("Vary", "Accept-Encoding");
            res.setContentType(findContentType(localFile));
            res.setContentLength(localData.length);

            res.setHeader("Connection", "keep-alive");
            ServletOutputStream sos = res.getOutputStream();
            sos.write(localData);
            sos.flush();
            sos.close();
        }
        else
        {
            send404(res);
        }
    }

    private String findContentType(File localFile)
    {
        String path = localFile.getAbsolutePath();
        if (path.endsWith(".js"))
        {
            return "application/x-javascript; charset=utf-8";
        } else if (path.endsWith(".css"))
        {
            return "text/css";
        }
        else if (path.endsWith(".html"))
        {
            return "text/html";
        }
        else
        {
            throw new IllegalArgumentException("Wrong extension: " + localFile.getAbsolutePath());
        }
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
