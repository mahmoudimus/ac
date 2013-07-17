package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Pair;
import com.google.common.base.Function;
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
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.atlassian.plugin.remotable.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.atlassian.plugin.remotable.test.HttpUtils.renderHtml;
import static com.atlassian.plugin.remotable.test.Utils.pickFreePort;
import static com.google.common.collect.Maps.newHashMap;

public class RemotePluginRunner
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Server server;
    private int port;
    private final Document doc;
    private final Map<String, HttpServlet> routes = newHashMap();
    private final String baseUrl;
    private final RemotePluginInstallerClient installer;
    private final String appKey;

    public RemotePluginRunner(String baseUrl, String appKey)
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
        installer = new RemotePluginInstallerClient(baseUrl, "admin", "admin");
    }

    public RemotePluginRunner addAdminPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("admin-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addAdminPage(String key, String name, String path, String resource, String section)
    {
        Element adminPage = doc.getRootElement().addElement("admin-page");
        adminPage
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        if (section != null)
        {
            adminPage.addAttribute("section", section);
        }

        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addConfigurePage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("configure-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        doc.getRootElement().element("plugin-info")
                .addElement("param")
                .addAttribute("name", "configure.url")
                .addText("/plugins/servlet" + createLocalUrl(doc.getRootElement().attributeValue("key"), path).toString()).getParent();
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addIssuePanelPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("issue-panel-page")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addIssueTabPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("issue-tab-page")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addProjectTabPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("project-tab-page")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addProjectConfigPanel(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("project-config-panel")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name)
                .addAttribute("location", "right");
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addProjectConfigTab(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("project-config-tab")
                .addAttribute("url", path)
                .addAttribute("key", key)
                .addAttribute("name", name)
                .addAttribute("weight", "10")
                .addAttribute("location", "projectgroup3");
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addDialogPage(String key, String name, String path, String resource, String section)
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
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addGeneralPage(String key, String name, String path, String resource)
    {
        return addGeneralPage(key, name, path, new MustacheServlet(resource));
    }

    public RemotePluginRunner addGeneralPage(String key, String name, String path, HttpServlet servlet)
    {
        return add(GeneralPageModule.key(key).name(name).path(path), servlet);
    }

    public RemotePluginRunner add(GeneralPageModule generalPageModule, String resource)
    {
        add(generalPageModule, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner add(GeneralPageModule generalPageModule, HttpServlet servlet)
    {
        generalPageModule.resource(servlet);
        generalPageModule.update(doc.getRootElement());

        addResource(generalPageModule);

        for (Condition condition : generalPageModule.conditions())
        {
            addResource(condition);
        }
        return this;
    }

    private void addResource(Module module)
    {
        module.getResource().map(new Function<Pair<String, HttpServlet>, Void>()
        {
            @Override
            public Void apply(Pair<String, HttpServlet> resource)
            {
                routes.put(resource.left(), resource.right());
                return null;
            }
        });
    }

    public RemotePluginRunner addOAuth(RunnerSignedRequestHandler signedRequestHandler) throws NoSuchAlgorithmException, IOException
    {
        doc.getRootElement().element("remote-plugin-container")
                .addElement("oauth")
                .addElement("public-key")
                .addText(signedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());

        return this;
    }

    public RemotePluginRunner addMacro(String macroKey, String path, HttpServlet servlet)
    {
        return addMacro(macroKey, path, servlet, Collections.<List<String>>emptyList());
    }

    public RemotePluginRunner addMacro(String macroKey, String path, HttpServlet servlet,
                                       List<List<String>> contextParameters)
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

    public RemotePluginRunner addWebhook(String hookId, String path, String eventId, HttpServlet servlet)
    {
        doc.getRootElement().addElement("webhook")
                .addAttribute("key", hookId + path.hashCode())
                .addAttribute("event", eventId)
                .addAttribute("url", path);
        routes.put(path, servlet);
        return this;
    }

    public RemotePluginRunner addSearchRequestView(String key, String name, String path,
                                                   String resource)
    {
        doc.getRootElement().addElement("remote-search-request-view")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addPermission(String apiScopeKey)
    {
        Element permissions = doc.getRootElement().element("plugin-info").element("permissions");
        if (permissions == null)
        {
            permissions = doc.getRootElement().element("plugin-info").addElement("permissions");
        }
        permissions.addElement("permission")
                .addText(apiScopeKey);
        return this;
    }

    public RemotePluginRunner addUnknownModule(String key)
    {
        doc.getRootElement().addElement("unknown")
                .addAttribute("key", key);
        return this;
    }

    public RemotePluginRunner description(String foo)
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

    public RemotePluginRunner start() throws Exception
    {
        port = pickFreePort();
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
            context.addServlet(new ServletHolder(entry.getValue()), entry.getKey());
        }

        list.addHandler(context);
        server.start();

        logger.debug("Started Atlassian Connect Add-On at '{}'", displayUrl);

        register();
        return this;
    }

    private class MustacheServlet extends HttpServlet
    {
        private final String path;

        public MustacheServlet(String path)
        {
            this.path = path;
        }

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
        {
            renderHtml(resp, path, new HashMap<String, Object>()
            {{
                    put("port", port);
                    put("baseurl", baseUrl);
                    put("clientKey", req.getParameter("oauth_consumer_key"));
                    put("locale", req.getParameter("loc"));
                    put("licenseStatus", req.getParameter("lic"));
                    put("timeZone", req.getParameter("tz"));
                }});
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
