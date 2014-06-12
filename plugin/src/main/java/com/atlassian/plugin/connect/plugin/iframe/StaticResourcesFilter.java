package com.atlassian.plugin.connect.plugin.iframe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.plugin.util.DevModeUtil.DEV_MODE_ENABLED;

/**
 * Provides static host resources for plugin iframes
 */
public class StaticResourcesFilter implements Filter
{
    public static final String HOST_RESOURCE_PATH = "/atlassian-connect";
    public static final int PLUGIN_TTL_NEAR_FUTURE = 60 * 30;               // 30 min
    public static final int AUI_TTL_FAR_FUTURE = 60 * 60 * 24 * 365;    // 1 year

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("(all(-debug)?\\.(js|css))|(aui/.*)");
    private static final Logger log = LoggerFactory.getLogger(StaticResourcesFilter.class);
    private static Plugin plugin;

    private FilterConfig config;
    private LoadingCache<String, CacheEntry> loadingCache;

    public StaticResourcesFilter(PluginRetrievalService pluginRetreivalService)
    {
        plugin = pluginRetreivalService.getPlugin();
    }

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        this.config = config;
        loadingCache = CacheBuilder.newBuilder()
                                   .build(new CacheLoader<String, CacheEntry>()
                                   {
                                       @Override
                                       public CacheEntry load(String s) throws Exception
                                       {
                                           return new CacheEntry(s);
                                       }
                                   });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // compute the starting resource path from the request
        String fullPath = req.getRequestURI().substring(req.getContextPath().length());

        // only serve resources in the host resource path, though this is precautionary only since no other
        // paths should be mapped to this filter in the first place
        if (!fullPath.startsWith(HOST_RESOURCE_PATH))
        {
            send404(fullPath, res);
            return;
        }

        // prepare a local path suitable for use with plugin.getResourceAsStream
        String localPath = fullPath.substring(HOST_RESOURCE_PATH.length() + 1);

        // only make selected resources available
        if (!RESOURCE_PATTERN.matcher(localPath).matches())
        {
            send404(fullPath, res);
            return;
        }

        // special dev mode case to make developing on all-debug.js not suck
        String encoding;
        CacheEntry entry;
        final String allDebugJsPath = "all-debug.js";
        if (allDebugJsPath.equals(localPath))
        {
            encoding = "identity";

            final String moduleDir = "js/iframe/";
            // note: any changes here must also be made in plugin/pom.xml!
            final String[] modules = {
                    "_amd.js",
                    "plugin/_util.js",
                    "plugin/_dollar.js",
                    "_base64.js",
                    "_events.js",
                    "_uri.js",
                    "_xdm.js",
                    "_ui-params.js",
                    "plugin/_rpc.js",
                    "plugin/events.js",
                    "plugin/env.js",
                    "plugin/request.js",
                    "plugin/dialog.js",
                    "plugin/inline-dialog.js",
                    "plugin/history.js",
                    "plugin/messages.js",
                    "plugin/cookie.js",
                    "plugin/_resize_listener.js",
                    "plugin/jira.js",
                    "plugin/confluence.js",
                    "plugin/_init.js"
            };
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            for (String module : modules)
            {
                bout.write(("/* " + module + " */\n").getBytes());
                InputStream in = plugin.getResourceAsStream(moduleDir + module);
                IOUtils.copy(in, bout);
                bout.write('\n');
            }

            entry = new CacheEntry(allDebugJsPath, bout.toByteArray());
        }
        else
        {
            if (req.getHeader("Accept-Encoding").contains("gzip"))
            {
                // check if the request accepts gzip, then get a gzipped version of the resource from the cache
                localPath += ".gz";
                encoding = "gzip";
            }
            else
            {
                encoding = "identity";
            }

            // ask the cache for an entry for the named resource
            try
            {
                entry = loadingCache.get(localPath);
            }
            catch (Exception e)
            {
                // if not found, 404
                send404(fullPath, res);
                return;
            }

            // the entry's data will be empty if the resource was not found
            if (entry.getData().length == 0)
            {
                // if not found, 404
                send404(fullPath, res);
                return;
            }
        }

        res.setContentType(entry.getContentType());
        res.setHeader("ETag", entry.getEtag());
        res.setHeader("Vary", "Accept-Encoding");
        setCacheControl(res, entry.getTTLSeconds());
        res.setHeader("Connection", "keep-alive");

        String previousToken = req.getHeader("If-None-Match");
        if (previousToken != null && previousToken.equals(entry.getEtag()))
        {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        else
        {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentLength(entry.getData().length);
            res.setHeader("Content-Encoding", encoding);
            ServletOutputStream sos = res.getOutputStream();
            sos.write(entry.getData());
            sos.flush();
            sos.close();
        }

        if (DEV_MODE_ENABLED)
        {
            loadingCache.invalidate(localPath);
        }
    }

    private void setCacheControl(HttpServletResponse res, int ttl)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        res.setDateHeader("Date", cal.getTimeInMillis());
        cal.add(Calendar.SECOND, ttl);
        res.setHeader("Cache-Control", "public, max-age=" + ttl);
        res.setDateHeader("Expires", cal.getTime().getTime());
    }

    private void send404(String path, HttpServletResponse res) throws IOException
    {
        res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find resource: " + path);
    }

    @Override
    public void destroy()
    {
        loadingCache.invalidateAll();
    }

    private class CacheEntry
    {
        private String contentType;
        private byte[] data;
        private String etag;
        private int ttl;

        public CacheEntry(String path, byte[] data)
        {
            setContentType(path);
            setData(data);
        }

        public CacheEntry(String path)
        {
            boolean gzip = path.endsWith(".gz");
            if (gzip)
            {
                path = path.substring(0, path.length() - 3);
            }

            setContentType(path);

            InputStream in;
            try
            {
                in = plugin.getResourceAsStream(path);
                if (in == null)
                {
                    clear();
                }
                else
                {
                    if (gzip)
                    {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        GZIPOutputStream out = new GZIPOutputStream(bytes);
                        IOUtils.copy(in, out);
                        out.finish();
                        out.close();
                        setData(bytes.toByteArray());
                    }
                    else
                    {
                        setData(IOUtils.toByteArray(in));
                    }
                }
            }
            catch (IOException e)
            {
                log.error("Unable to retrieve content: " + path, e);
                clear();
            }

            ttl = path.startsWith("aui/") ? AUI_TTL_FAR_FUTURE : PLUGIN_TTL_NEAR_FUTURE;
        }

        private void setContentType(String path)
        {
            contentType = config.getServletContext().getMimeType(path);
            // covers anything not mapped in default servlet context config, such as web fonts
            if (contentType == null)
            {
                contentType = "application/octet-stream";
            }
        }

        private void setData(byte[] data)
        {
            this.data = data;
            this.etag = DigestUtils.md5Hex(data);
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

        public int getTTLSeconds()
        {
            return ttl;
        }

        private void clear()
        {
            data = new byte[0];
            etag = "";
        }
    }
}
