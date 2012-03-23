package com.atlassian.labs.remoteapps.test;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.io.XMLWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.test.HttpUtils.renderHtml;
import static com.atlassian.labs.remoteapps.test.Utils.pickFreePort;
import static com.atlassian.labs.remoteapps.util.EncodingUtils.encodeBase64;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 11/01/12
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteAppRunner
{
    private Server server;
    private final Document doc;
    private final Map<String,String> routes = newHashMap();
    private final int port;
    private final String baseUrl;
    private final RemoteAppInstallerClient installer;
    private final String appKey;

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
        installer = new RemoteAppInstallerClient(baseUrl, "betty", "betty");
    }

    public RemoteAppRunner addAdminPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("admin-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, resource);
        return this;
    }

    public RemoteAppRunner addGeneralPage(String key, String name, String path, String resource)
    {
        doc.getRootElement().addElement("general-page")
                .addAttribute("url", path)
                .addAttribute("name", name)
                .addAttribute("key", key);
        routes.put(path, resource);
        return this;
    }

    public RemoteAppRunner start() throws Exception
    {
        return start("");
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

    public RemoteAppRunner start(String secret) throws Exception
    {
        server = new Server(port);
        HandlerList list = new HandlerList();
        server.setHandler(list);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new DescriptorServlet(secret)), "/register");

        for (final Map.Entry<String,String> entry : routes.entrySet())
        {
            context.addServlet(new ServletHolder(new MustacheServlet(entry.getValue())), entry.getKey());
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
