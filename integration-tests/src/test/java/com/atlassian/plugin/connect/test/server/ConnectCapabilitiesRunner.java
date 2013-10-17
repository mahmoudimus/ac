package com.atlassian.plugin.connect.test.server;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.test.Environment;
import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.Utils;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.bouncycastle.openssl.PEMWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth.signature.RSA_SHA1;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @since version
 */
public class ConnectCapabilitiesRunner
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String baseUrl;
    private final AtlassianConnectRestClient installer;
    private final ConnectAddonBeanBuilder addonBuilder;
    private final String pluginKey;
    private Option<? extends SignedRequestHandler> signedRequestHandler;
    private ConnectAddonBean addon;
    private OAuthBean oAuthBean;
    
    private int port;
    private Server server;
    private final Map<String, HttpServlet> routes = newHashMap();
    
    public ConnectCapabilitiesRunner(String baseUrl, String pluginKey)
    {
        this.baseUrl = checkNotNull(baseUrl);
        this.pluginKey = checkNotNull(pluginKey);
        
        this.addonBuilder = newConnectAddonBean()
                .withKey(pluginKey)
                .withName(pluginKey)
                .withVersion("1.0");
        
        this.installer = new AtlassianConnectRestClient(baseUrl, "admin", "admin");
    }

    private void register() throws Exception
    {
        installer.install("http://localhost:" + port + "/register");
    }

    public void uninstall() throws Exception
    {
        installer.uninstall(addon.getKey());
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

    public ConnectCapabilitiesRunner addCapability(CapabilityBean bean)
    {
        addonBuilder.withCapability(bean);
        return this;
    }

    public ConnectCapabilitiesRunner addRoute(String path, HttpServlet servlet)
    {
        routes.put(path, servlet);
        return this;
    }

    public ConnectCapabilitiesRunner addOAuth() throws NoSuchAlgorithmException, IOException
    {
        return addOAuth(createSignedRequestHandler(pluginKey));
    }

    public ConnectCapabilitiesRunner addOAuth(RunnerSignedRequestHandler signedRequestHandler) throws NoSuchAlgorithmException, IOException
    {
        this.signedRequestHandler = some(signedRequestHandler);
        
        this.oAuthBean = newOAuthBean()
                .withPublicKey(signedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString())
                .build();

        //return addPermission(Permissions.CREATE_OAUTH_LINK);
        return this;
    }

    public static RunnerSignedRequestHandler createSignedRequestHandler(String appKey) throws NoSuchAlgorithmException, IOException
    {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        KeyPair oauthKeyPair = gen.generateKeyPair();
        StringWriter publicKeyWriter = new StringWriter();
        PEMWriter pubWriter = new PEMWriter(publicKeyWriter);
        pubWriter.writeObject(oauthKeyPair.getPublic());
        pubWriter.close();

        StringWriter privateKeyWriter = new StringWriter();
        PEMWriter privWriter = new PEMWriter(privateKeyWriter);
        privWriter.writeObject(oauthKeyPair.getPrivate());
        privWriter.close();

        Environment env = new TestEnv();
        env.setEnv("OAUTH_LOCAL_PUBLIC_KEY", publicKeyWriter.toString());
        env.setEnv("OAUTH_LOCAL_PRIVATE_KEY", privateKeyWriter.toString());
        env.setEnv("OAUTH_LOCAL_KEY", appKey);
        return new RunnerSignedRequestHandler(appKey, env);
    }
    
    public ConnectCapabilitiesRunner start() throws Exception
    {
        port = Utils.pickFreePort();
        final String displayUrl = "http://localhost:" + port;
        
        if(null != oAuthBean)
        {
            addonBuilder.withCapability(
                    newRemoteContainerBean()
                    .withDisplayUrl(displayUrl)
                    .withOAuth(oAuthBean)
                    .build()
            );
        }
        else
        {
            addonBuilder.withCapability(
                    newRemoteContainerBean()
                            .withDisplayUrl(displayUrl)
                            .build()
            );
        }
        
        this.addon = addonBuilder.build();

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

        System.out.println("Started Atlassian Connect Add-On at " + displayUrl);
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
            String json = CapabilitiesGsonFactory.getGson().toJson(addon);
            response.getWriter().write(json);
            response.getWriter().close();
        }
    }

    private static class TestEnv implements Environment
    {
        private Map<String, String> env = Maps.newHashMap();

        @Override
        public String getEnv(String name)
        {
            return env.get(name);
        }

        @Override
        public String getOptionalEnv(String name, String def)
        {
            final String value = env.get(name);
            return value == null ? def : value;
        }

        @Override
        public void setEnv(String name, String value)
        {
            env.put(name, value);
        }

        @Override
        public void setEnvIfNull(String name, String value)
        {
            if (env.get(name) == null)
            {
                setEnv(name, value);
            }
        }
    }
}
