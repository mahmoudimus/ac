package com.atlassian.plugin.connect.test.server;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.gson.DefaultModuleSerializer;
import com.atlassian.plugin.connect.plugin.installer.ModuleListDeserializer;
import com.atlassian.plugin.connect.plugin.installer.StaticAvailableModuleTypes;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.Utils;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.servlet.ConnectAppServlets;
import it.servlet.ContextServlet;
import it.servlet.HttpContextServlet;
import it.servlet.InstallHandlerServlet;
import it.servlet.condition.ToggleableConditionServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private static final Logger log = LoggerFactory.getLogger(ConnectRunner.class);

    private final String productBaseUrl;
    private final AtlassianConnectRestClient installer;
    private final ConnectAddonBeanBuilder addonBuilder;
    private final String pluginKey;
    private final Set<ScopeName> scopes = new HashSet<ScopeName>();

    private ToggleableConditionServlet toggleableConditionServlet;
    private SignedRequestHandler signedRequestHandler;
    private ConnectAddonBean addon;
    private StaticAvailableModuleTypes moduleTypes = new StaticAvailableModuleTypes();

    private int port;
    private Server server;
    private final Map<String, HttpServlet> routes = newHashMap();
    private boolean checkInstallationStatus = true;

    /**
     * Create a ConnectRunner for an add-on with randomly generated key
     * @param testedProduct the product to install the add-on into
     */
    public ConnectRunner(TestedProduct testedProduct)
    {
        this(testedProduct, AddonTestUtils.randomAddOnKey());
    }

    /**
     * Create a ConnectRunner for an add-on with a specified key
     * @param testedProduct the product to install the add-on into
     * @param key the key for the add-on
     */
    public ConnectRunner(TestedProduct testedProduct, String key)
    {
        this(testedProduct.getProductInstance().getBaseUrl(), key);
    }

    /**
     * Create a ConnectRunner for an add-on with a specified key
     * @param productBaseUrl the url of the product to install the add-on into
     * @param key the key for the add-on
     */
    public ConnectRunner(String productBaseUrl, String key)
    {
        this.productBaseUrl = checkNotNull(productBaseUrl);
        this.pluginKey = checkNotNull(key);

        this.addonBuilder = newConnectAddonBean()
                .withKey(key)
                .withName(key)
                .withVersion("1.0");

        this.installer = new AtlassianConnectRestClient(productBaseUrl, "admin", "admin");
    }

    public void register() throws Exception
    {
        URI host = URI.create(this.productBaseUrl);
        installer.install("http://" + host.getHost() + ":" + port + REGISTRATION_ROUTE, checkInstallationStatus);
    }

    public void uninstall() throws Exception
    {
        installer.uninstall(addon.getKey());
    }

    public void setAddonEnabled(boolean enabled) throws Exception
    {
        installer.setEnabled(addon.getKey(), enabled);
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

    public ConnectRunner setAuthenticationToNone()
    {
        addonBuilder.withAuthentication(AuthenticationBean.none());
        return this;
    }

    public ConnectRunner setVendor(final VendorBean vendor)
    {
        addonBuilder.withVendor(vendor);
        return this;
    }

    public ConnectRunner disableInstallationStatusCheck()
    {
        checkInstallationStatus = false;
        return this;
    }

    public ConnectRunner addModule(String fieldName, ModuleBean bean)
    {
        addonBuilder.withModule(fieldName, bean);
        return this;
    }

    public ConnectRunner addModules(String fieldName, ModuleBean... beans)
    {
        addonBuilder.withModules(fieldName, beans);
        return this;
    }
    
    public ConnectRunner addModuleMeta(ConnectModuleMeta meta)
    {
        moduleTypes.addModuleMeta(meta);
        return this;
    }

    public ConnectRunner addRoute(String path, HttpServlet servlet)
    {
        if (routes.containsKey(path))
        {
            throw new IllegalArgumentException(String.format("The path '%s' already exists!", path));
        }

        routes.put(path, servlet);
        return this;
    }

    public ConnectRunner addJWT()
    {
        return addJWT(ConnectAppServlets.installHandlerServlet());
    }

    public ConnectRunner addJWT(InstallHandlerServlet installHandlerServlet)
    {
        return addJWT(installHandlerServlet, createJwtSignedRequestHandler(installHandlerServlet));
    }

    public ConnectRunner addJWT(HttpServlet installHandlerServlet)
    {
        return addJWT(installHandlerServlet, null);
    }

    private ConnectRunner addJWT(HttpServlet installHandlerServlet, SignedRequestHandler signedRequestHandler)
    {
        addonBuilder.withAuthentication(AuthenticationBean.newAuthenticationBean()
                .withType(AuthenticationType.JWT)
                .build());
        addInstallLifecycle();
        this.signedRequestHandler = signedRequestHandler;
        addRoute(INSTALLED_PATH, installHandlerServlet);
        return this;
    }

    private SignedRequestHandler createJwtSignedRequestHandler(final InstallHandlerServlet installHandlerServlet)
    {
        return new SignedRequestHandler()
        {

            @Override
            public void sign(URI uri, String method, String username, HttpURLConnection connection)
            {
                try
                {
                    final String sharedSecret = checkNotNull(installHandlerServlet.getInstallPayload().getSharedSecret());
                    final String jwt = AddonTestUtils.generateJwtSignature(HttpMethod.valueOf(method), uri, addonBuilder.getKey(), sharedSecret, productBaseUrl, null);
                    connection.setRequestProperty("Authorization", "JWT " + jwt);
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e);
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public ConnectRunner addScopes(ScopeName... scopesToAdd)
    {
        Collections.addAll(scopes, scopesToAdd);
        return this;
    }

    public ConnectRunner addScope(ScopeName scopeName)
    {
        scopes.add(scopeName);
        return this;
    }

    public SignedRequestHandler getSignedRequestHandler()
    {
        return signedRequestHandler;
    }

    public ConnectRunner start() throws Exception
    {
        URI host = URI.create(this.productBaseUrl);
        port = Utils.pickFreePort();
        final String displayUrl ="http://" + host.getHost() + ':' + port;

        addonBuilder.withBaseurl(displayUrl);
        addonBuilder.withScopes(scopes);

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

        toggleableConditionServlet = new ToggleableConditionServlet(true);
        context.addServlet(new ServletHolder(toggleableConditionServlet), ToggleableConditionServlet.TOGGLE_CONDITION_URL);

        list.addHandler(context);
        server.start();

        log.info("Started Atlassian Connect Add-On at " + displayUrl + REGISTRATION_ROUTE);
        register();
        return this;
    }

    public static HttpServlet newServlet(ContextServlet servlet)
    {
        return new HttpContextServlet(servlet);
    }

    /**
     * @return a {@link org.junit.rules.TestRule} that reverts the condition back to it's initial value.
     */
    public TestRule resetToggleableConditionRule()
    {
        return toggleableConditionServlet.resetToInitialValueRule();
    }

    public void setToggleableConditionShouldDisplay(boolean shouldDisplay)
    {
        toggleableConditionServlet.setShouldDisplay(shouldDisplay);
    }

    private ImmutableMap<String, Object> getBaseContext()
    {
        return ImmutableMap.<String, Object>of("port", port, "baseurl", productBaseUrl);
    }

    private class DescriptorServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            String json = getGson().toJson(addon);
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getWriter().write(json);
            response.getWriter().close();
        }
        
        private Gson getGson()
        {
            GsonBuilder builder = ConnectModulesGsonFactory.getGsonBuilder();
            if (moduleTypes.hasMetas())
            {
                builder = builder.registerTypeAdapter(ConnectModulesGsonFactory.getModuleJsonType(), new ModuleListDeserializer(moduleTypes));
            }
            else
            {
                builder = builder.registerTypeAdapter(ConnectModulesGsonFactory.getModuleJsonType(), new DefaultModuleSerializer());
            }
            return builder.create();
        }
    }
}
