package it.capabilities;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.Value;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import com.google.common.collect.Lists;
import it.AbstractBrowserlessTest;
import it.servlet.ConnectAppServlets;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean.newPageBean;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestUpgrade extends AbstractBrowserlessTest
{
    private static final String PLUGIN_KEY = "my-upgraded-plugin";

    private ConnectCapabilitiesRunner plugin0;
    private ConnectCapabilitiesRunner plugin1;

    /**
     * Check that ACDEV-651 hasn't regressed.
     */
    @Test
    public void testPluginModulesDoNotRiseFromTheDead() throws Exception
    {
        // install then uninstall a plugin
        plugin0 = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Page One", null))
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet());
        plugin0.start().stopAndUninstall();
        plugin0 = null;

        // install another plugin with the same key, but different modules
        plugin1 = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Page Two", null))
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet());
        plugin1.start();

        // check that the plugin only has two modules: a web item and servlet corresponding to the "Page Two" generalPage
        JSON pluginJson = JSON.parse(plugin1.getUpmPluginJson());
        Value modules = pluginJson.get("modules");
        assertThat(modules.getValueType(), is(Value.TYPE.ARRAY));
        assertThat(modules.size(), is(2));
        List<String> moduleKeys = Lists.newArrayList(
            modules.get(0).get("key").getString(),
            modules.get(1).get("key").getString()
        );
        assertThat(moduleKeys, hasItems("page-two", "servlet-page-two"));

        plugin1.stopAndUninstall();
        plugin1 = null;
    }

    @After
    public void uninstallPlugin1() throws Exception
    {
        for (ConnectCapabilitiesRunner plugin : Lists.newArrayList(plugin0, plugin1))
        {
            if (plugin != null)
            {
                plugin.stopAndUninstall();
            }
        }
    }

}
