package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginState;
import com.google.common.base.Strings;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecyclePluginHelper;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleUpmHelper;
import it.com.atlassian.plugin.connect.testlifecycle.util.LifecycleTestAuthenticator;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.junit.Assert.fail;

public class AbstractPluginLifecycleTest
{

    public static final String JSON_TEMPLATE_PREFIX = "/json/";

    private static final Logger log = LoggerFactory.getLogger(AbstractPluginLifecycleTest.class);

    protected final PluginController pluginController;
    protected final LifecyclePluginHelper pluginHelper;
    protected final LifecycleUpmHelper upmHelper;
    protected final LifecycleTestAuthenticator testAuthenticator;

    protected Plugin theConnectPlugin;

    public AbstractPluginLifecycleTest(PluginController pluginController,
            LifecyclePluginHelper pluginHelper,
            LifecycleUpmHelper upmHelper,
            LifecycleTestAuthenticator testAuthenticator)
    {
        this.pluginController = pluginController;
        this.pluginHelper = pluginHelper;
        this.upmHelper = upmHelper;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setupBeforeAllTests()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @After
    public void tearDown()
    {
        if (null != theConnectPlugin)
        {
            try
            {
                pluginController.uninstall(theConnectPlugin);
            }
            catch (Exception e)
            {
                log.warn("Failed to uninstall plugin", e);
            }
            finally
            {
                theConnectPlugin = null;
            }
        }
    }

    protected Plugin installAndEnableAddon(String template) throws IOException
    {
        Plugin plugin = upmHelper.installAddon(getAddonJson(template));
        upmHelper.getUpmControlHandler().enablePlugins(plugin.getKey());
        return plugin;
    }

    protected String getAddonJson(String template) throws IOException
    {
        InputStream is = this.getClass().getResourceAsStream(AbstractPluginLifecycleTest.JSON_TEMPLATE_PREFIX + template);
        String json = IOUtils.toString(is);
        return json;
    }

    protected void assertStateAndModuleCount(Plugin originalAddonPlugin, PluginState state, int moduleCount, String prefix)
    {
        StringBuilder sb = new StringBuilder();

        if (!Strings.isNullOrEmpty(prefix))
        {
            sb.append(prefix).append(" - ");
        }

        sb.append(originalAddonPlugin.getKey()).append(": ");

        Plugin pluginToCheck = upmHelper.getUpmControlHandler().getPlugin(originalAddonPlugin.getKey());

        PluginState addonState = pluginToCheck.getPluginState();
        Collection<ModuleDescriptor<?>> addonModules = pluginToCheck.getModuleDescriptors();
        int addonModuleCount = addonModules.size();

        boolean failed = false;
        if (!state.equals(addonState))
        {
            sb.append("expected state ").append(state.name()).append(" but was ").append(addonState.name());
            failed = true;
        }
        if (moduleCount != addonModuleCount)
        {
            sb.append(", expected module size ").append(moduleCount).append(" but was ").append(addonModuleCount);
            failed = true;
        }

        if (failed)
        {
            fail(sb.toString());
        }
    }
}
