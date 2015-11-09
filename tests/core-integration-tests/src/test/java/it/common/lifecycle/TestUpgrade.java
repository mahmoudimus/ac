package it.common.lifecycle;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.product.TestedProductAccessor;

import com.google.common.collect.Lists;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.Value;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.matcher.ValueMatchers.hasProperty;
import static it.matcher.ValueMatchers.isArrayMatching;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class TestUpgrade
{
    private final String baseUrl = TestedProductAccessor.get().getTestedProduct().getProductInstance().getBaseUrl();

    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String KEY_PAGE_ONE = "page-one";
    private static final String KEY_PAGE_TWO = "page-two";

    private ConnectRunner plugin0;
    private ConnectRunner plugin1;

    /**
     * Check that ACDEV-651 hasn't regressed.
     */
    @Test
    public void testPluginModulesDoNotRiseFromTheDead() throws Exception
    {
        // install then uninstall a plugin
        plugin0 = new ConnectRunner(baseUrl, PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Page One", null))
                                .withKey(KEY_PAGE_ONE)
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet());
        plugin0.start().stopAndUninstall();
        plugin0 = null;

        // install another plugin with the same key, but different modules
        plugin1 = new ConnectRunner(baseUrl, PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Page Two", null))
                                .withKey(KEY_PAGE_TWO)
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet());
        plugin1.start();

        JSON pluginJson = JSON.parse(plugin1.getUpmPluginJson());
        Matcher<Iterable<? super Value>> valMatcher = hasItem(
                hasProperty("key", addonAndModuleKey(PLUGIN_KEY,KEY_PAGE_TWO)));

        assertThat(pluginJson.get("modules"), isArrayMatching(valMatcher));

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
