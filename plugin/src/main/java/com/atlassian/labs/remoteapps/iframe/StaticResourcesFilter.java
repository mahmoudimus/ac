package com.atlassian.labs.remoteapps.iframe;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.util.PluginUtils;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Provides the aggregated js for iframes
 */
public class StaticResourcesFilter implements Filter
{
    // todo: support languages
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("/[a-zA-Z0-9]+\\.(?:js|css)");
    private static Map<String,CacheEntry> resCache = new MapMaker().makeComputingMap(new Function<String, CacheEntry>() {

        @Override
        public CacheEntry apply(String from)
        {
            return new CacheEntry(from);
        }
    });
    private static Plugin plugin;
    private final boolean devMode;

    public StaticResourcesFilter(PluginRetrievalService pluginRetreivalService)
    {
        plugin = pluginRetreivalService.getPlugin();
        devMode = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length() + "/remoteapps".length());
        if (RESOURCE_PATTERN.matcher(path).matches())
        {
            String localPath = path.substring(1);
            if (req.getHeader("Accept-Encoding").contains("gzip"))
            {
                localPath += ".gz";
            }
            CacheEntry entry = resCache.get(localPath);
            if (entry.getData().length == 0)
            {
                send404(path, res);
                return;
            }
            res.setHeader("ETag", entry.getEtag());
            res.setHeader("Content-Encoding", "gzip");
            res.setHeader("Vary", "Accept-Encoding");
            res.setContentType(entry.getContentType());
            res.setContentLength(entry.getData().length);
            setCacheControl(res);

  	        String previousToken = req.getHeader("If-None-Match");
            if (previousToken != null && previousToken.equals(entry.getEtag()))
            {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
            else
            {
                res.setHeader("Connection", "keep-alive");
                ServletOutputStream sos = res.getOutputStream();
                sos.write(entry.getData());
                sos.flush();
                sos.close();
            }
            if (devMode)
            {
                resCache.remove(localPath);
            }
        }
        else
        {
            send404(path, res);
        }
    }

    private void setCacheControl(HttpServletResponse res)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        res.setDateHeader("Date", cal.getTimeInMillis());
        int expiry = 30 * 60;
        cal.add(Calendar.SECOND, expiry);
        res.setHeader("Cache-Control", "public, max-age=" + expiry);
        res.setDateHeader("Expires", cal.getTime().getTime());
    }

    private void send404(String path, HttpServletResponse res) throws IOException
    {
        res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find resource '" + path + "'");
    }

    @Override
    public void destroy()
    {
        resCache.clear();
    }

    private static class CacheEntry
    {
        private String etag;
        private String contentType;
        private byte[] data;

        public CacheEntry(String path)
        {
            InputStream in = null;
            try
            {
                in = plugin.getResourceAsStream(path);
                if (in == null)
                {
                    data = new byte[0];
                    etag = "";
                }
                else
                {
                    data = IOUtils.toByteArray(in);
                    etag = DigestUtils.md5Hex(data);
                }

            }
            catch (IOException e)
            {
                // todo: record exception
                data = new byte[0];
                etag = "";
            }
            if (path.endsWith(".js"))
            {
                contentType = "application/x-javascript; charset=utf-8";
            } else if (path.endsWith(".css"))
            {
                contentType = "text/css";
            }
        }

        public String getEtag()
        {
            return etag;
        }

        public byte[] getData()
        {
            return data;
        }

        public String getContentType()
        {
            return contentType;
        }
    }
}
