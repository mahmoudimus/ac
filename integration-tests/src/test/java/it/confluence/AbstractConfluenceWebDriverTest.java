package it.confluence;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.maven.MavenDependencyHelper;
import com.atlassian.confluence.it.maven.MavenUploadablePlugin;
import com.atlassian.confluence.it.plugin.Plugin;
import com.atlassian.confluence.it.plugin.PluginHelper;
import com.atlassian.confluence.it.plugin.SimplePlugin;
import com.atlassian.confluence.it.plugin.UploadablePlugin;
import com.atlassian.confluence.it.plugin.WebTestPluginHelper;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.confluence.it.rpc.StartOfTestLogger;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.webdriver.testing.rule.IgnoreBrowserRule;
import com.atlassian.webdriver.testing.rule.TestBrowserRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

/**
 * This is an adapted version of com.atlassian.confluence.webdriver.AbstractWebDriverTest. It works with the AC test DB
 * and default host/port. Installing the 'scripts finished' plugin makes all of Confluence's page objects available to
 * tests, without forcing us to create "Fixed" versions of them that simply override a wait condition.
 */
public class AbstractConfluenceWebDriverTest
{
    public static final Space DEMO_SPACE = new Space("ds", "Demonstration Space");

    @Rule public IgnoreBrowserRule ignoreRule = new IgnoreBrowserRule();
    @Rule public TestBrowserRule testBrowserRule = new TestBrowserRule();
    @Rule public WebDriverScreenshotRule webDriverScreenshotRule = new WebDriverScreenshotRule();

    @Rule public TestName name = new TestName();

    protected static final ConfluenceTestedProduct product = TestedProductFactory.create(FixedConfluenceTestedProduct.class);
    protected static final ConfluenceRpc rpc = ConfluenceRpc.newInstance(product.getProductInstance().getBaseUrl(), ConfluenceRpc.Version.V2_WITH_WIKI_MARKUP);

    private static final Dimension DEFAULT_SCREEN_SIZE = new Dimension(1024, 768);

    private static final LazyReference<UploadablePlugin> FUNCTEST_RPC_PLUGIN_HOLDER = new LazyReference<UploadablePlugin>()
    {
        @Override
        protected UploadablePlugin create() throws Exception
        {
            return resolveFuncTestRpcPlugin();
        }
    };

    private static final LazyReference<UploadablePlugin> SCRIPTS_FINISHED_PLUGIN = new LazyReference<UploadablePlugin>()
    {
        @Override
        protected UploadablePlugin create() throws Exception
        {
            return resolveScriptsFinishedPlugin();
        }
    };

    @Before
    public void start() throws Exception
    {
        product.deleteAllCookies();
        // we have some deadlock issues with workbox and cleaning up test data so make sure we are not on a confluence first
//        product.visit(NoOpPage.class);

        rpc.logIn(User.ADMIN);
        installTestPlugins(rpc);

        // DFE hangs the Chrome WebDriver tests.
        // So, it's disabled for now.
        rpc.getPluginHelper().disablePlugin(new SimplePlugin("com.atlassian.confluence.confluence-editor-hide-tools", null));

        rpc.getDarkFeaturesHelper().enableSiteFeature("webdriver.test.mode");
        disableFeatureDiscovery();

        StartOfTestLogger.instance().logTestStart(rpc, getClass(), name.getMethodName());

        // set our window up to be the default screen size
        WebDriver.Window window = product.getTester().getDriver().manage().window();
        if (!DEFAULT_SCREEN_SIZE.equals(window.getSize()))
            window.setSize(DEFAULT_SCREEN_SIZE);
    }

    @After
    public void clear()
    {
        rpc.logIn(User.ADMIN);
        rpc.getDarkFeaturesHelper().disableSiteFeature("webdriver.test.mode");
    }

    private void disableFeatureDiscovery()
    {
        Plugin helpTipsPlugin = new SimplePlugin("com.atlassian.plugins.atlassian-help-tips", "Atlassian Help Tips");
        rpc.getPluginHelper().disablePlugin(helpTipsPlugin);
    }

    // The below is copied from com.atlassian.confluence.webdriver.WebDriverSetupTest,
    // which doesn't use the ConfluenceRpc base url

    private static void installTestPlugins(ConfluenceRpc rpc) throws Exception
    {
        PluginHelper pluginHelper = rpc.getPluginHelper();
        if (!pluginHelper.isPluginEnabled(FUNCTEST_RPC_PLUGIN_HOLDER.get()))
        {
            new WebTestPluginHelper(rpc.getBaseUrl(), User.ADMIN).installPlugin(FUNCTEST_RPC_PLUGIN_HOLDER.get());
        }

        if (!pluginHelper.isPluginEnabled(SCRIPTS_FINISHED_PLUGIN.get()))
        {
            pluginHelper.installPlugin(SCRIPTS_FINISHED_PLUGIN.get());
        }
    }

    private static UploadablePlugin resolveFuncTestRpcPlugin()
    {
        return new MavenUploadablePlugin("confluence.extra.functestrpc",
                "Confluence Functional Test Remote API",
                MavenDependencyHelper.resolve("com.atlassian.confluence.plugins", "confluence-functestrpc-plugin"));
    }
    private static UploadablePlugin resolveScriptsFinishedPlugin()
    {
        return new MavenUploadablePlugin("com.atlassian.confluence.plugins.confluence-scriptsfinished-plugin",
                "Confluence Scripts Finished Plugin",
                MavenDependencyHelper.resolve("com.atlassian.confluence.plugins", "confluence-scriptsfinished-plugin"));
    }
}
