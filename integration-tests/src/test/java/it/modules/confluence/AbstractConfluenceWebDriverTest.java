package it.modules.confluence;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.maven.MavenDependencyHelper;
import com.atlassian.confluence.it.maven.MavenUploadablePlugin;
import com.atlassian.confluence.it.plugin.*;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.confluence.it.rpc.StartOfTestLogger;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConnectMacroBrowserDialog;
import com.atlassian.util.concurrent.LazyReference;
import it.ConnectWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * This is an adapted version of com.atlassian.confluence.webdriver.AbstractWebDriverTest. It works with the AC test DB
 * and default host/port. Installing the 'scripts finished' plugin makes all of Confluence's page objects available to
 * tests, without forcing us to create "Fixed" versions of them that simply override a wait condition.
 */
public class AbstractConfluenceWebDriverTest extends ConnectWebDriverTestBase
{
    public static class TestSpace
    {
        public static Space DEMO = new Space("ds", "Demonstration Space");
    }

    public static class TestUser
    {
        public static User ADMIN = User.ADMIN;
    }

    @Rule
    public TestName name = new TestName();

    protected static final ConfluenceRpc rpc = ConfluenceRpc.newInstance(product.getProductInstance().getBaseUrl(), ConfluenceRpc.Version.V2_WITH_WIKI_MARKUP);

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

    protected static ConfluenceTestedProduct getProduct()
    {
        return (ConfluenceTestedProduct) product;
    }

    protected ConfluenceOps confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());

    @BeforeClass
    public static void confluenceTestSetup() throws Exception
    {
        rpc.logIn(User.ADMIN);
        installTestPlugins(rpc);

        // Hangs the Chrome WebDriver tests, so it's disabled for now.
        rpc.getPluginHelper().disablePlugin(new SimplePlugin("com.atlassian.confluence.confluence-editor-hide-tools", null));

        rpc.getDarkFeaturesHelper().enableSiteFeature("webdriver.test.mode");

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
        StartOfTestLogger.instance().logTestStart(rpc, getClass(), name.getMethodName());
    }

    private static void disableFeatureDiscovery()
    {
        Plugin helpTipsPlugin = new SimplePlugin("com.atlassian.plugins.atlassian-help-tips", "Atlassian Help Tips");
        rpc.getPluginHelper().disablePlugin(helpTipsPlugin);
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

    protected void selectMacro(CreatePage editorPage, String macroName)
    {
        MacroBrowserDialog macroBrowser = editorPage.openMacroBrowser();
        ConnectMacroBrowserDialog connectMacroBrowserDialog = connectPageOperations.findConnectMacroBrowserDialog();
        MacroItem macro = macroBrowser.searchForFirst(macroName);
        MacroForm macroForm = macro.select();
        macroForm.waitUntilVisible();
        connectMacroBrowserDialog.clickSave();
    }

}