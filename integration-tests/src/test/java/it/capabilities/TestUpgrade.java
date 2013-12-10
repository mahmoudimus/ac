package it.capabilities;

import cc.plural.jsonij.JSON;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.collect.Lists;
import it.AbstractBrowserlessTest;
import it.servlet.ConnectAppServlets;
import org.junit.After;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean.newPageBean;
import static it.matcher.ValueMatchers.hasProperty;
import static it.matcher.ValueMatchers.isArrayMatching;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class TestUpgrade extends AbstractBrowserlessTest
{
    private static final String PLUGIN_KEY = "my-upgraded-plugin";

    private ConnectRunner plugin0;
    private ConnectRunner plugin1;

    /**
     * Check that ACDEV-651 hasn't regressed.
     */
    @Test
    public void testPluginModulesDoNotRiseFromTheDead() throws Exception
    {
        // install then uninstall a plugin
        plugin0 = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
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
        plugin1 = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
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
        assertThat(pluginJson.get("modules"), isArrayMatching(
                containsInAnyOrder(
                        hasProperty("key", "page-two"),
                        hasProperty("key", "servlet-page-two"))));

        plugin1.stopAndUninstall();
        plugin1 = null;
    }

    @After
    public void uninstallPlugin1() throws Exception
    {
        for (ConnectRunner plugin : Lists.newArrayList(plugin0, plugin1))
        {
            if (plugin != null)
            {
                plugin.stopAndUninstall();
            }
        }
    }

}
