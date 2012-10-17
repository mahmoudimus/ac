package com.atlassian.plugin.remotable.test;

import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.remotable.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.atlassian.plugin.remotable.test.HttpUtils.renderHtml;
import static com.atlassian.plugin.remotable.test.Utils.pickFreePort;
import static com.atlassian.plugin.remotable.plugin.util.EncodingUtils.encodeBase64;
import static com.google.common.collect.Maps.newHashMap;

public class RemotePluginRunner
{
    private Server server;
    private final Document doc;
    private final Map<String,HttpServlet> routes = newHashMap();
    private final int port;
    private final String baseUrl;
    private final RemotePluginInstallerClient installer;
    private final String appKey;
    private String secret;

    public RemotePluginRunner(String baseUrl, String appKey)
    {
        this.baseUrl = baseUrl;
        this.appKey = appKey;
        port = pickFreePort();
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
                        .addAttribute("display-url", "http://localhost:" + port)
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

    public RemotePluginRunner addGeneralPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("general-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemotePluginRunner addOAuth(RunnerSignedRequestHandler signedRequestHandler) throws NoSuchAlgorithmException, IOException
    {
        doc.getRootElement().element("remote-plugin-container")
                .addElement("oauth")
                    .addElement("public-key")
                        .addText(signedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());

        return this;
    }

    public RemotePluginRunner addGeneralPage(String key, String name, String path, HttpServlet servlet)
    {
        doc.getRootElement().addElement("general-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, servlet);
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

    public RemotePluginRunner addWebhook(String hookId, String path, HttpServlet servlet)
    {
        doc.getRootElement().addElement("webhook")
                .addAttribute("key", hookId + path.hashCode())
                .addAttribute("event", hookId)
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

    public RemotePluginRunner secret(String secret)
    {
        this.secret = secret;
        return this;
    }

    private void register(String secret) throws IOException
    {
        installer.install("http://localhost:" + port + "/register", secret);
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
        server = new Server(port);
        HandlerList list = new HandlerList();
        server.setHandler(list);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new DescriptorServlet(secret)), "/register");

        for (final Map.Entry<String,HttpServlet> entry : routes.entrySet())
        {
            context.addServlet(new ServletHolder(entry.getValue()), entry.getKey());
        }

        list.addHandler(context);
        server.start();

        register(secret);
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
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            renderHtml(resp, path, new HashMap<String, Object>()
            {{
                put("port", port);
                put("baseurl", baseUrl);
            }});
        }
    }

    private class DescriptorServlet extends HttpServlet
    {
        private final String secret;

        public DescriptorServlet(String secret)
        {
            this.secret = secret;
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            String regAuth = request.getHeader("Authorization");
            if (("RemotePluginRegistration secret=" + encodeBase64(secret)).equals(regAuth))
            {
                new XMLWriter(response.getWriter()).write(doc);
                response.getWriter().close();
            }
            else
            {
                response.sendError(500, "Invalid authorization: " + regAuth);
            }
        }
    }
}
