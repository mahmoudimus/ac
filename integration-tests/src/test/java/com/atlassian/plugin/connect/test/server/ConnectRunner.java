package com.atlassian.plugin.connect.test.server;

import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.test.Environment;
import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.Utils;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.servlet.ContextServlet;
import it.servlet.HttpContextServlet;
import net.oauth.signature.RSA_SHA1;
import org.bouncycastle.openssl.PEMWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @since 1.0
 */
public class ConnectRunner
{
    public static final String INSTALLED_PATH = "/installed-lifecycle";
    public static final String ENABLED_PATH = "/enabled-lifecycle";
    public static final String DISABLED_PATH = "/disabled-lifecycle";
    public static final String UNINSTALLED_PATH = "/uninstalled-lifecycle";
    private static final String REGISTRATION_ROUTE = "/register";

    private final String baseUrl;
    private final AtlassianConnectRestClient installer;
    private final ConnectAddonBeanBuilder addonBuilder;
    private final String pluginKey;
    private SignedRequestHandler signedRequestHandler;
    private ConnectAddonBean addon;
    
    private int port;
    private Server server;
    private final Map<String, HttpServlet> routes = newHashMap();

    public ConnectRunner(String baseUrl, String pluginKey)
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
        installer.install("http://localhost:" + port + REGISTRATION_ROUTE);
    }

    public void uninstall() throws Exception
    {
        installer.uninstall(addon.getKey());
    }

    public ConnectAddonBean getAddon()
    {
        return addon;
    }

    /**
     * @return the UPM's JSON representation of this add-on.
     */
    public String getUpmPluginJson() throws Exception
    {
        return installer.getUpmPluginJson(addon.getKey());
    }

    public void stopRunnerServer() throws Exception
    {
        server.stop();
    }

    public void stopAndUninstall() throws Exception
    {
        stopRunnerServer();
        uninstall();
    }

    public static void stopAndUninstallQuietly(ConnectRunner runner)
    {
        if (runner != null)
        {
            try
            {
                runner.stopAndUninstall();
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }

    public ConnectRunner addInstallLifecycle()
    {
        LifecycleBean lifecycle = getLifecycle();
        addonBuilder.withLifecycle(newLifecycleBean(lifecycle).withInstalled(INSTALLED_PATH).build());
        return this;
    }

    public ConnectRunner addEnableLifecycle()
    {
        LifecycleBean lifecycle = getLifecycle();
        addonBuilder.withLifecycle(newLifecycleBean(lifecycle).withEnabled(ENABLED_PATH).build());
        return this;
    }

    public ConnectRunner addDisableLifecycle()
    {
        LifecycleBean lifecycle = getLifecycle();
        addonBuilder.withLifecycle(newLifecycleBean(lifecycle).withDisabled(DISABLED_PATH).build());
        return this;
    }

    public ConnectRunner addUninstallLifecycle()
    {
        LifecycleBean lifecycle = getLifecycle();
        addonBuilder.withLifecycle(newLifecycleBean(lifecycle).withUninstalled(UNINSTALLED_PATH).build());
        return this;
    }

    private LifecycleBean getLifecycle()
    {
        if (null == addonBuilder.getLifecycle())
        {
            addonBuilder.withLifecycle(newLifecycleBean().build());
        }

        return addonBuilder.getLifecycle();
    }

    public ConnectRunner enableLicensing()
    {
        addonBuilder.withLicensing(true);
        return this;
    }
    
    public ConnectRunner addModule(String fieldName, ModuleBean bean)
    {
        addonBuilder.withModule(fieldName, bean);
        return this;
    }

    public ConnectRunner addCapabilities(String fieldName, ModuleBean... beans)
    {
        addonBuilder.withModules(fieldName, beans);
        return this;
    }

    public ConnectRunner addRoute(String path, HttpServlet servlet)
    {
        routes.put(path, servlet);
        return this;
    }

    public ConnectRunner addOAuth() throws NoSuchAlgorithmException, IOException
    {
        return addOAuth(createSignedRequestHandler(pluginKey));
    }

    public ConnectRunner addOAuth(RunnerSignedRequestHandler signedRequestHandler) throws NoSuchAlgorithmException, IOException
    {
        this.signedRequestHandler = signedRequestHandler;

        addonBuilder.withAuthentication(newAuthenticationBean().withType(AuthenticationType.OAUTH).withPublicKey(signedRequestHandler.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString()).build());

        //return addPermission(Permissions.CREATE_OAUTH_LINK);
        return this;
    }

    public SignedRequestHandler getSignedRequestHandler()
    {
        return signedRequestHandler;
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

    public ConnectRunner start() throws Exception
    {
        port = Utils.pickFreePort();
        final String displayUrl = "http://localhost:" + port;

        addonBuilder.withBaseurl(displayUrl);

        this.addon = addonBuilder.build();

        server = new Server(port);
        HandlerList list = new HandlerList();
        server.setHandler(list);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new DescriptorServlet()), REGISTRATION_ROUTE);

        for (final Map.Entry<String, HttpServlet> entry : routes.entrySet())
        {
            if (entry.getValue() instanceof HttpContextServlet)
            {
                ((HttpContextServlet) entry.getValue()).getBaseContext().putAll(getBaseContext());
            }
            context.addServlet(new ServletHolder(entry.getValue()), entry.getKey());
        }

        list.addHandler(context);
        server.start();

        System.out.println("Started Atlassian Connect Add-On at " + displayUrl + REGISTRATION_ROUTE);
        register();
        return this;
    }

    public static HttpServlet newServlet(ContextServlet servlet)
    {
        return new HttpContextServlet(servlet);
    }

    public static HttpServlet newMustacheServlet(String resource)
    {
        return newServlet(new MustacheServlet(resource));
    }

    private ImmutableMap<String, Object> getBaseContext()
    {
        return ImmutableMap.<String, Object>of("port", port, "baseurl", baseUrl);
    }

    private static final class MustacheServlet extends ContextServlet
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
            String json = ConnectModulesGsonFactory.getGson().toJson(addon);
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getWriter().write(json);
            response.getWriter().close();
        }
    }

    private static class TestEnv implements Environment
    {
        private Map<String, String> env = newHashMap();

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
