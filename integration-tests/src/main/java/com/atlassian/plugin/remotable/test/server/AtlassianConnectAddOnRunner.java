package com.atlassian.plugin.remotable.test.server;

import com.atlassian.fugue.Pair;
import com.atlassian.plugin.remotable.test.HttpUtils;
import com.atlassian.plugin.remotable.test.Utils;
import com.atlassian.plugin.remotable.test.client.AtlassianConnectRestClient;
import com.atlassian.plugin.remotable.test.server.module.Module;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.oauth.signature.RSA_SHA1;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public final class AtlassianConnectAddOnRunner
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Server server;
    private int port;
    private final Document doc;
    private final Map<String, HttpServlet> routes = newHashMap();
    private final String baseUrl;
    private final AtlassianConnectRestClient installer;
    private final String appKey;

    public AtlassianConnectAddOnRunner(String baseUrl, String appKey)
    {
        this.baseUrl = baseUrl;
        this.appKey = appKey;
        doc = DocumentFactory.getInstance().createDocument()
                .addElement("atlassian-plugin")
                .addAttribute("key", appKey)
                .addAttribute("name", appKey)
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

    public AtlassianConnectAddOnRunner addIssueTabPage(String key, String name, String path, HttpServlet resource)
    {
        doc.getRootElement().addElement("issue-tab-page")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name);
        routes.put(path, resource);
        return this;
    }

    public AtlassianConnectAddOnRunner addProjectTabPage(String key, String name, String path, HttpServlet resource)
    {
        doc.getRootElement().addElement("project-tab-page")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name);
        routes.put(path, resource);
        return this;
    }

    public AtlassianConnectAddOnRunner addProjectConfigPanel(String key, String name, String path, HttpServlet resource)
    {
        doc.getRootElement().addElement("project-config-panel")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name)
                .addAttribute("location", "right");
        routes.put(path, resource);
        return this;
    }

    public AtlassianConnectAddOnRunner addProjectConfigTab(String key, String name, String path, HttpServlet resource)
    {
        doc.getRootElement().addElement("project-config-tab")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name)
                .addAttribute("weight", "10")
                .addAttribute("location", "projectgroup3");
        routes.put(path, resource);
        return this;
    }

    public AtlassianConnectAddOnRunner addDialogPage(String key, String name, String path, HttpServlet resource, String section)
    {
        Element dialogPage = doc.getRootElement().addElement("dialog-page");
        dialogPage
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);

        if (section != null)
        {
            dialogPage.addAttribute("section", section);
        }
        routes.put(path, resource);
        return this;
    }

    public AtlassianConnectAddOnRunner add(Module module)
    {
        module.update(doc.getRootElement());
        addResources(module);
        return this;
    }

    private void addResources(Module module)
    {
        for (Pair<String, HttpServlet> resource : module.getResources())
        {
            routes.put(resource.left(), resource.right());
        }
    }

    public AtlassianConnectAddOnRunner addOAuth(RunnerSignedRequestHandler signedRequestHandler) throws NoSuchAlgorithmException, IOException
    {
        doc.getRootElement().element("remote-plugin-container")
                .addElement("oauth")
                .addElement("public-key")
                .addText(signedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());

        return this;
    }

    public AtlassianConnectAddOnRunner addMacro(String macroKey, String path, HttpServlet servlet)
    {
        return addMacro(macroKey, path, servlet, Collections.<List<String>>emptyList());
    }

    public AtlassianConnectAddOnRunner addMacro(String macroKey, String path, HttpServlet servlet, List<List<String>> contextParameters)
    {
        Element macro = doc.getRootElement().addElement("remote-macro")
                .addAttribute("key", macroKey)
                .addAttribute("url", path);
        if (!contextParameters.isEmpty())
        {
            Element params = macro.addElement("context-parameters");
            for (List<String> param : contextParameters)
            {
                params.addElement("context-parameter")
                        .addAttribute("name", param.get(0))
                        .addAttribute("type", param.get(1));
            }
        }
        routes.put(path, servlet);
        return this;
    }

    public AtlassianConnectAddOnRunner addWebhook(String hookId, String path, String eventId, HttpServlet servlet)
    {
        doc.getRootElement().addElement("webhook")
                .addAttribute("key", hookId + path.hashCode())
                .addAttribute("event", eventId)
                .addAttribute("url", path);
        routes.put(path, servlet);
        return this;
    }

    public AtlassianConnectAddOnRunner addSearchRequestView(String key, String name, String path, HttpServlet resource)
    {
        doc.getRootElement().addElement("remote-search-request-view")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, resource);
        return this;
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

    private void register() throws IOException
    {
        installer.install("http://localhost:" + port + "/register");
    }

    private void unregister() throws IOException
    {
        installer.uninstall(appKey);
    }

    public void stop() throws Exception
    {
        server.stop();
        unregister();
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

        private ImmutableMap<String, Object> getContext(HttpServletRequest req)
        {
            return ImmutableMap.<String, Object>builder()
                    .putAll(baseContext)
                    .put("clientKey", req.getParameter("oauth_consumer_key"))
                    .put("locale", req.getParameter("loc"))
                    .put("licenseStatus", req.getParameter("lic"))
                    .put("timeZone", req.getParameter("tz"))
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
