package com.atlassian.plugin.connect.test.server;

import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.Utils;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import com.atlassian.plugin.connect.test.server.module.Module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.lang.RandomStringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth.signature.RSA_SHA1;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.Utils.createSignedRequestHandler;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;

public final class AtlassianConnectAddOnRunner
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String baseUrl;
    private final String pluginKey;
    private final Document doc;
    private final AtlassianConnectRestClient installer;
    private final Map<String, HttpServlet> routes = newHashMap();

    private int port;
    private Server server;
    private Option<? extends SignedRequestHandler> signedRequestHandler;

    public AtlassianConnectAddOnRunner(String baseUrl)
    {
        // Converting 3->4 is a temporary workaround for JRADEV-23912
        this(baseUrl, RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase());
    }

    public AtlassianConnectAddOnRunner(String baseUrl, String pluginKey)
    {
        this.baseUrl = checkNotNull(baseUrl);
        this.pluginKey = checkNotNull(pluginKey);

        this.doc = DocumentFactory.getInstance().createDocument()
                                  .addElement("atlassian-plugin")
                                  .addAttribute("key", pluginKey)
                                  .addAttribute("name", pluginKey)
                                  .addAttribute("plugins-version", "2")
                                  .addElement("plugin-info")
                                  .addElement("version").addText("1").getParent()
                                  .getParent()
                                  .addElement("remote-plugin-container")
                                  .addAttribute("key", "container")
                                  .getParent()
                                  .getDocument();

        installer = new AtlassianConnectRestClient(baseUrl, "admin", "admin");
    }

    public AtlassianConnectAddOnRunner add(Module module)
    {
        module.update(doc.getRootElement());
        addResources(module);
        return this;
    }

    public AtlassianConnectAddOnRunner addRoute(String path, HttpServlet servlet)
    {
        routes.put(path, servlet);
        return this;
    }

    private void addResources(Module module)
    {
        for (Pair<String, HttpServlet> resource : module.getResources())
        {
            routes.put(resource.left(), resource.right());
        }
    }

    public AtlassianConnectAddOnRunner addOAuth() throws NoSuchAlgorithmException, IOException
    {
        return addOAuth(createSignedRequestHandler(pluginKey));
    }

    public AtlassianConnectAddOnRunner addOAuth(RunnerSignedRequestHandler signedRequestHandler) throws NoSuchAlgorithmException, IOException
    {
        this.signedRequestHandler = some(signedRequestHandler);
        doc.getRootElement().element("remote-plugin-container")
           .addElement("oauth")
           .addElement("public-key")
           .addText(signedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());

        return addPermission(Permissions.CREATE_OAUTH_LINK);
    }

    public AtlassianConnectAddOnRunner addPermission(String apiScopeKey)
    {
        Element permissions = doc.getRootElement().element("plugin-info").element("permissions");
        if (permissions == null)
        {
            permissions = doc.getRootElement().element("plugin-info").addElement("permissions");
        }
        permissions.addElement("permission").addText(apiScopeKey);
        return this;
    }

    public AtlassianConnectAddOnRunner addUnknownModule(String key)
    {
        doc.getRootElement().addElement("unknown")
           .addAttribute("key", key);
        return this;
    }

    public AtlassianConnectAddOnRunner description(String foo)
    {
        doc.getRootElement().element("plugin-info").elements().add(0,
                DocumentFactory.getInstance().createElement("description").addText(foo));
        return this;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public Option<? extends SignedRequestHandler> getSignedRequestHandler()
    {
        return signedRequestHandler;
    }

    private void register() throws IOException
    {
        installer.install("http://localhost:" + port + "/register");
    }

    public void uninstall() throws IOException
    {
        installer.uninstall(pluginKey);
    }

    public void stopRunnerServer() throws Exception
    {
        server.stop();
    }

    public void stop() throws Exception
    {
        stopRunnerServer();
        uninstall();
    }

    public AtlassianConnectAddOnRunner start() throws Exception
    {
        port = Utils.pickFreePort();
        final String displayUrl = "http://localhost:" + port;
        doc.getRootElement().element("remote-plugin-container").addAttribute("display-url", displayUrl);

        server = new Server(port);
        HandlerList list = new HandlerList();
        server.setHandler(list);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new DescriptorServlet()), "/register");

        for (final Map.Entry<String, HttpServlet> entry : routes.entrySet())
        {
            if (entry.getValue() instanceof WithContextHttpServlet)
            {
                ((WithContextHttpServlet) entry.getValue()).baseContext.putAll(getBaseContext());
            }
            context.addServlet(new ServletHolder(entry.getValue()), entry.getKey());
        }

        list.addHandler(context);
        server.start();

        StringWriter writer = new StringWriter();
        new XMLWriter(writer).write(doc);
        logger.debug("Started Atlassian Connect Add-On at '{}' with descriptor:\n{}", displayUrl, writer);

        register();
        return this;
    }

    public static HttpServlet newServlet(WithContextServlet servlet)
    {
        return new WithContextHttpServlet(servlet);
    }

    public static HttpServlet newMustacheServlet(String resource)
    {
        return newServlet(new MustacheServlet(resource));
    }

    private ImmutableMap<String, Object> getBaseContext()
    {
        return ImmutableMap.<String, Object>of("port", port, "baseurl", baseUrl);
    }

    public static class WithContextServlet
    {
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
        {
        }

        protected void doPost(final HttpServletRequest req, final HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
        {
        }
    }

    private static class WithContextHttpServlet extends HttpServlet
    {
        private final Map<String, Object> baseContext = Maps.newHashMap();
        private final WithContextServlet servlet;

        private WithContextHttpServlet(WithContextServlet servlet)
        {
            this.servlet = checkNotNull(servlet);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            servlet.doGet(req, resp, getContext(req));
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            servlet.doPost(req, resp, getContext(req));
        }

        private ImmutableMap<String, Object> getContext(HttpServletRequest req) throws IOException
        {
            return ImmutableMap.<String, Object>builder()
                               .putAll(baseContext)
                               .put("req_url", nullToEmpty(option(req.getRequestURL()).getOrElse(new StringBuffer()).toString()))
                               .put("req_uri", nullToEmpty(req.getRequestURI()))
                               .put("req_query", nullToEmpty(req.getQueryString()))
                               .put("req_method", req.getMethod())
                               .put("clientKey", nullToEmpty(req.getParameter("oauth_consumer_key")))
                               .put("locale", nullToEmpty(req.getParameter("loc")))
                               .put("licenseStatus", nullToEmpty(req.getParameter("lic")))
                               .put("timeZone", nullToEmpty(req.getParameter("tz")))
                               .build();
        }
    }

    private static final class MustacheServlet extends WithContextServlet
    {
        private final String path;

        private MustacheServlet(String path)
        {
            this.path = checkNotNull(path);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
        {
            HttpUtils.renderHtml(resp, path, context);
        }
    }

    private class DescriptorServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            new XMLWriter(response.getWriter()).write(doc);
            response.getWriter().close();
        }
    }
}
