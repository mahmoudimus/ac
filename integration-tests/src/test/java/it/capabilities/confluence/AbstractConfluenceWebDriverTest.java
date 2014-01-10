package it.capabilities.confluence;

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
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.webdriver.testing.rule.IgnoreBrowserRule;
import com.atlassian.webdriver.testing.rule.TestBrowserRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
    public static class TestSpace {
        public static Space DEMO = new Space("ds", "Demonstration Space");
    }

    public static class TestUser {
        public static User ADMIN = User.ADMIN;
        public static User BETTY = new User("betty", "betty", "Betty Admin", "betty@example.com");
        public static User BARNEY = new User("barney", "barney", "Barney User", "barney@example.com");
        public static User FRED = new User("fred", "fred", "Fred Sysadmin", "fred@example.com");
    }

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

    private static final LazyReference<UploadablePlugin> SCRIPTS_FINISHED_PLUGIN_HOLDER = new LazyReference<UploadablePlugin>()
    {
        @Override
        protected UploadablePlugin create() throws Exception
        {
            return resolveScriptsFinishedPlugin();
        }
    };

    protected ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    @BeforeClass
    public static void confluenceTestSetup() throws Exception
    {
        rpc.logIn(User.ADMIN);
        installTestPlugins(rpc);

        // Hangs the Chrome WebDriver tests, so it's disabled for now.
        rpc.getPluginHelper().disablePlugin(new SimplePlugin("com.atlassian.confluence.confluence-editor-hide-tools", null));

        rpc.getDarkFeaturesHelper().enableSiteFeature("webdriver.test.mode");

        disableLicenseReminder();
        disableFeatureDiscovery();
    }

    @AfterClass
    public static void confluenceTestTeardown() throws Exception
    {
        rpc.logIn(User.ADMIN);
        rpc.getDarkFeaturesHelper().disableSiteFeature("webdriver.test.mode");
    }

    @Before
    public void setupTest() throws Exception
    {
        product.deleteAllCookies();

        StartOfTestLogger.instance().logTestStart(rpc, getClass(), name.getMethodName());

        // set our window up to be the default screen size
        WebDriver.Window window = product.getTester().getDriver().manage().window();
        if (!DEFAULT_SCREEN_SIZE.equals(window.getSize()))
            window.setSize(DEFAULT_SCREEN_SIZE);
    }

    private static void disableFeatureDiscovery()
    {
        Plugin helpTipsPlugin = new SimplePlugin("com.atlassian.plugins.atlassian-help-tips", "Atlassian Help Tips");
        rpc.getPluginHelper().disablePlugin(helpTipsPlugin);
    }

    private static void disableLicenseReminder() throws Exception
    {
        String username = User.ADMIN.getUsername();
        String password = User.ADMIN.getPassword();

        AtlassianConnectRestClient client = new AtlassianConnectRestClient(rpc.getBaseUrl(), username, password);

        HttpPost post = new HttpPost(rpc.getBaseUrl() + "/rest/stp/1.0/license/remindMeNever");
        post.addHeader("Accept", "*/*");

        client.sendRequestAsUser(post, new BasicResponseHandler(), username, password);
    }

    // The three methods below are copied from com.atlassian.confluence.webdriver.WebDriverSetupTest,
    // which unfortunately doesn't use the ConfluenceRpc base url

    private static void installTestPlugins(ConfluenceRpc rpc) throws Exception
    {
        PluginHelper pluginHelper = rpc.getPluginHelper();
        if (!pluginHelper.isPluginEnabled(FUNCTEST_RPC_PLUGIN_HOLDER.get()))
        {
            new WebTestPluginHelper(rpc.getBaseUrl(), User.ADMIN).installPlugin(FUNCTEST_RPC_PLUGIN_HOLDER.get());
        }

        if (!pluginHelper.isPluginEnabled(SCRIPTS_FINISHED_PLUGIN_HOLDER.get()))
        {
            pluginHelper.installPlugin(SCRIPTS_FINISHED_PLUGIN_HOLDER.get());
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
