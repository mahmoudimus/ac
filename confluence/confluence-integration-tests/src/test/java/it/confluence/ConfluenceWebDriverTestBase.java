package it.confluence;

import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.confluence.it.rpc.StartOfTestLogger;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.confluence.pageobjects.component.dialog.MacroForm;
import com.atlassian.confluence.pageobjects.component.dialog.MacroItem;
import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.confluence.pageobjects.component.editor.toolbars.InsertDropdownMenu;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.content.Editor;
import com.atlassian.confluence.pageobjects.page.content.EditorPage;
import com.atlassian.confluence.test.BaseUrlSelector;
import com.atlassian.confluence.test.ConfluenceBaseUrlSelector;
import com.atlassian.confluence.test.plugin.DefaultPluginHelper;
import com.atlassian.confluence.test.plugin.PluginHelper;
import com.atlassian.confluence.test.plugin.SimplePlugin;
import com.atlassian.confluence.test.rest.ConfluenceJacksonClientBuilder;
import com.atlassian.confluence.test.rpc.VersionedRpcBaseResolver;
import com.atlassian.confluence.test.rpc.api.ConfluenceRpcClient;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceEditorContent;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceInsertMenu;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluencePageOperations;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.common.util.ConnectTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;
import com.atlassian.plugin.connect.test.confluence.util.ConfluenceTestUserFactory;
import com.atlassian.testutils.annotations.Retry;
import com.atlassian.testutils.junit.RetryRule;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.confluence.api.model.content.ContentRepresentation.STORAGE;
import static com.atlassian.confluence.api.model.content.ContentType.PAGE;
import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;
import static it.confluence.ConfluenceWebDriverTestBase.TestSpace.DEMO;

/**
 * This is an adapted version of com.atlassian.confluence.webdriver.AbstractWebDriverTest.
 * It works with the AC test DB and default host/port. Installing the 'scripts
 * finished' plugin makes all of Confluence's page objects available to tests,
 * without forcing us to create "Fixed" versions of them that simply override a
 * wait condition.
 */
@Retry(maxAttempts=ConfluenceWebDriverTestBase.MAX_RETRY_ATTEMPTS)
public class ConfluenceWebDriverTestBase
{
    protected static final ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();
    protected static final ConfluenceRpc rpc = ConfluenceRpc.newInstance(product.getProductInstance().getBaseUrl(), ConfluenceRpc.Version.V2_WITH_WIKI_MARKUP);
    protected static ConfluenceRestClient restClient;
    protected static ConnectTestUserFactory testUserFactory;
    protected static ConfluencePageOperations confluencePageOperations = new ConfluencePageOperations(
            product.getPageBinder(), product.getTester().getDriver());
    private final Logger logger = LoggerFactory.getLogger(ConfluenceWebDriverTestBase.class);

    private boolean hasBeenFocused;

    private static final BaseUrlSelector urlSelector = new ConfluenceBaseUrlSelector();
    private static final ConfluenceRpcClient rpcClient = new ConfluenceRpcClient(urlSelector, VersionedRpcBaseResolver.V2);
    private static final Client client = ConfluenceJacksonClientBuilder.newClient();
    private static final com.atlassian.confluence.test.rest.api.ConfluenceRestClient internalRestClient = new com.atlassian.confluence.test.rest.api.ConfluenceRestClient(urlSelector, client);
    private static final PluginHelper pluginHelper = new DefaultPluginHelper(rpcClient, internalRestClient);

    public static class TestSpace
    {
        public static Space DEMO = new Space("ds", "Demonstration Space");
    }

    public static class MacroBrowserAndEditor
    {
        public final MacroBrowserDialog browserDialog;
        public final MacroItem macro;
        public final MacroForm macroForm;

        public MacroBrowserAndEditor(MacroBrowserDialog browserDialog, MacroItem macro, MacroForm macroForm)
        {
            this.browserDialog = browserDialog;
            this.macroForm = macroForm;
            this.macro = macro;
        }
    }

    @Rule
    public TestName name = new TestName();

    protected static ConfluenceTestedProduct getProduct()
    {
        return product;
    }

    protected ConfluenceOps confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    @Rule
    public RetryRule retryRule = new RetryRule();
    public static final int MAX_RETRY_ATTEMPTS = 3;

    @BeforeClass
    public static void confluenceTestSetup() throws Exception
    {
        testUserFactory = new ConfluenceTestUserFactory(product, rpc);
        final TestUser admin = testUserFactory.admin();
        rpc.logIn(toConfluenceUser(admin));
        restClient = new ConfluenceRestClient(getProduct(), admin);

        // Hangs the Chrome WebDriver tests, so it's disabled for now.
        try
        {
            pluginHelper.disablePlugin(new SimplePlugin("com.atlassian.confluence.confluence-editor-hide-tools", null));
        }
        catch (UniformInterfaceException ignored)
        {
            // Missing or already disabled. Carry on.
        }

        product.getPageBinder().override(EditorContent.class, ConfluenceEditorContent.class);
        product.getPageBinder().override(InsertDropdownMenu.class, ConfluenceInsertMenu.class);

        rpc.getDarkFeaturesHelper().enableSiteFeature("webdriver.test.mode");
    }

    @AfterClass
    public static void confluenceTestTeardown() throws Exception
    {
        rpc.logIn(toConfluenceUser(testUserFactory.admin()));
        rpc.getDarkFeaturesHelper().disableSiteFeature("webdriver.test.mode");
    }

    @Before
    public void setupTest() throws Exception
    {
        StartOfTestLogger.instance().logTestStart(rpc, getClass(), name.getMethodName());
    }

    protected MacroBrowserAndEditor selectMacro(CreatePage editorPage, String macroName)
    {
        return selectMacro(editorPage, macroName, null);
    }

    protected MacroBrowserAndEditor selectMacro(CreatePage editorPage, String macroName, @Nullable Runnable macroDialogSubmitter)
    {
        editorPage.dismissEditorNotifications();
        MacroBrowserAndEditor browserAndEditor = findMacroInBrowser(editorPage, macroName);

        if (null == browserAndEditor.macro)
        {
            return browserAndEditor;
        }
        else
        {
            MacroForm macroForm = browserAndEditor.macro.select();

            if (null != macroDialogSubmitter)
            {
                macroDialogSubmitter.run();
            }
            else
            {
                macroForm.waitUntilVisible();
            }

            return new MacroBrowserAndEditor(browserAndEditor.browserDialog, browserAndEditor.macro, macroForm);
        }
    }

    protected void selectMacroAndSave(CreatePage editorPage, String macroName)
    {
        MacroBrowserDialog browserDialog = selectMacro(editorPage, macroName).browserDialog;
        saveSelectedMacro(browserDialog);
    }

    protected void selectMacroAndSave(MacroBrowserAndEditor macroBrowserAndEditor)
    {
        MacroForm macroForm = macroBrowserAndEditor.macro.select();
        macroForm.waitUntilVisible();
        saveSelectedMacro(macroBrowserAndEditor.browserDialog);
    }

    private void saveSelectedMacro(MacroBrowserDialog browserDialog)
    {
        Poller.waitUntilTrue(browserDialog.isSaveButtonEnabled());
        browserDialog.clickSave();
        browserDialog.waitUntilHidden();
    }

    protected MacroBrowserAndEditor findMacroInBrowser(CreatePage editorPage, String macroName)
    {
        final Editor editor = editorPage.getEditor();
        enableMacrosDropdown(editorPage);
        final InsertDropdownMenu insertDropdownMenu = editor.openInsertMenu();
        insertDropdownMenu.waitUntilVisible();
        MacroBrowserDialog macroBrowserDialog = insertDropdownMenu.clickInsertMacro();
        MacroItem macro = macroBrowserDialog.searchForFirst(macroName);
        return new MacroBrowserAndEditor(macroBrowserDialog, macro, null);
    }

    protected void enableMacrosDropdown(CreatePage editorPage)
    {
        editorPage.dismissEditorNotifications();
        if (!hasBeenFocused)
        {
            hasBeenFocused = true;
            editorPage.getContent().focus();
        }
    }

    protected Runnable macroDialogSubmitter(final String moduleKey)
    {
        return () -> {
            RemotePluginDialog dialog = confluencePageOperations.findDialog(moduleKey);
            dialog.submitAndWaitUntilHidden();
        };
    }

    protected Runnable macroDialogCanceller(final String moduleKey)
    {
        return () -> {
            RemotePluginDialog dialog = confluencePageOperations.findDialog(moduleKey);
            dialog.cancelAndWaitUntilHidden();
        };
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected static void login(TestUser user)
    {
        logout();
        product.visit(LoginPage.class).login(user.getUsername(), user.getPassword(), HomePage.class);
    }

    protected static <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        logout();
        return product.login(toConfluenceUser(user), page, args);
    }

    public static <T> T runWithAnonymousUsePermission(Callable<T> test) throws Exception
    {
        rpc.grantAnonymousUsePermission();
        try
        {
            return test.call();
        }
        finally
        {
            rpc.revokeAnonymousUsePermission();
        }
    }

    protected void cancelEditor(EditorPage editorPage)
    {
        try
        {
            product.getPageBinder().bind(editorPage.getClass()).cancel();
        }
        catch (Throwable t)
        {
            logger.warn(t.getMessage());
        }
    }

    protected static Content createPage(String title, String storageFormat)
    {
        Content content = Content.builder(PAGE)
                .space(DEMO.getKey())
                .title(title)
                .body(storageFormat, STORAGE)
                .build();
        return restClient.content().create(content).claim();
    }
}
