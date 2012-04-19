package com.atlassian.labs.remoteapps.test;

import org.bouncycastle.openssl.PEMWriter;
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
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.labs.remoteapps.apputils.Environment.setEnv;
import static com.atlassian.labs.remoteapps.test.HttpUtils.renderHtml;
import static com.atlassian.labs.remoteapps.test.Utils.pickFreePort;
import static com.atlassian.labs.remoteapps.util.EncodingUtils.encodeBase64;
import static com.google.common.collect.Maps.newHashMap;

public class RemoteAppRunner
{
    private Server server;
    private final Document doc;
    private final Map<String,HttpServlet> routes = newHashMap();
    private final int port;
    private final String baseUrl;
    private final RemoteAppInstallerClient installer;
    private final String appKey;
    private String secret;
    private boolean stripUnknownModules;

    public RemoteAppRunner(String baseUrl, String appKey)
    {
        this.baseUrl = baseUrl;
        this.appKey = appKey;
        port = pickFreePort();
        doc = DocumentFactory.getInstance().createDocument()
                .addElement("remote-app")
                    .addAttribute("key", appKey)
                    .addAttribute("name", appKey)
                    .addAttribute("version", "1")
                    .addAttribute("display-url", "http://localhost:" + port)
                .getDocument();
        installer = new RemoteAppInstallerClient(baseUrl, "admin", "admin");
    }

    public RemoteAppRunner addAdminPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("admin-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemoteAppRunner addConfigurePage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("configure-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemoteAppRunner addGeneralPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("general-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, new MustacheServlet(resource));
        return this;
    }

    public RemoteAppRunner addOAuth() throws NoSuchAlgorithmException, IOException
    {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        KeyPair oauthKeyPair = gen.generateKeyPair();
        StringWriter publicKeyWriter = new StringWriter();
        PEMWriter pubWriter = new PEMWriter(publicKeyWriter);
        pubWriter.writeObject(oauthKeyPair.getPublic());
        pubWriter.close();

        doc.getRootElement().addElement("oauth")
                .addElement("public-key")
                    .addText(publicKeyWriter.toString());

        StringWriter privateKeyWriter = new StringWriter();
        PEMWriter privWriter = new PEMWriter(privateKeyWriter);
        privWriter.writeObject(oauthKeyPair.getPrivate());
        privWriter.close();

        setEnv("OAUTH_LOCAL_PUBLIC_KEY", publicKeyWriter.toString());
        setEnv("OAUTH_LOCAL_PRIVATE_KEY", privateKeyWriter.toString());
        setEnv("OAUTH_LOCAL_KEY", doc.getRootElement().attributeValue("key"));

        return this;
    }

    public RemoteAppRunner addGeneralPage(String key, String name, String path, HttpServlet servlet)
    {
        doc.getRootElement().addElement("general-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, servlet);
        return this;
    }

    public RemoteAppRunner addPermission(String apiScopeKey)
    {
        Element permissions = doc.getRootElement().element("permissions");
        if (permissions == null)
        {
            permissions = doc.getRootElement().addElement("permissions");
        }
        permissions.addElement("permission")
                .addAttribute("scope", apiScopeKey);
        return this;
    }

    public RemoteAppRunner addUnknownModule(String key)
    {
        doc.getRootElement().addElement("unknown")
                .addAttribute("key", key);
        return this;
    }

    public RemoteAppRunner description(String foo)
    {
        doc.getRootElement().addElement("description").setText(foo);
        return this;
    }

    public RemoteAppRunner secret(String secret)
    {
        this.secret = secret;
        return this;
    }

    public RemoteAppRunner stripUnknownModules()
    {
        this.stripUnknownModules = true;
        return this;
    }

    private void register(String secret, boolean stripUnknownModules) throws IOException
    {
        installer.install("http://localhost:" + port + "/register", secret, stripUnknownModules);
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

    public RemoteAppRunner start() throws Exception
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

        register(secret, stripUnknownModules);
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
            if (("RemoteAppsRegistration secret=" + encodeBase64(secret)).equals(regAuth))
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
