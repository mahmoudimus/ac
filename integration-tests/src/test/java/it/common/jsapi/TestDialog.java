package it.common.jsapi;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.modules.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDialog extends MultiProductWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_GENERALPAGE_WEBITEM_DIALOG = "general-page-opening-webitem-dialog";
    private static final String ADDON_WEBITEM_DIALOG = "my-webitem-dialog";
    private static final String REMOTE_PLUGIN_DIALOG_KEY = "remotePluginDialog";
    private static final String SIZE_TO_PARENT_DIALOG_KEY = "sizeToParentDialog";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("AA", null))
                                .withUrl("/pg")
                                .withKey(ADDON_GENERALPAGE)
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty("BB", null))
                                .withUrl("/my-dialog-url?myuserid={user.id}")
                                .withKey(ADDON_DIALOG)
                                .withLocation("none")
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty("CC", null))
                                .withUrl("/general-page")
                                .withKey(ADDON_GENERALPAGE_WEBITEM_DIALOG)
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("DD", null))
                                .withUrl("/my-webitem-dialog?myuserid={user.id}")
                                .withKey(ADDON_WEBITEM_DIALOG)
                                .withLocation("none")
                                .withContext(AddOnUrlContext.addon)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .build(),
                        newWebItemBean()
                                .withKey(REMOTE_PLUGIN_DIALOG_KEY)
                                .withName(new I18nProperty("EE", null))
                                .withUrl("/rpd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(SIZE_TO_PARENT_DIALOG_KEY)
                                .withName(new I18nProperty("SzP", null))
                                .withUrl("/fsd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )

                .addRoute("/pg", ConnectAppServlets.openDialogServlet())
                .addRoute("/my-dialog-url", ConnectAppServlets.closeDialogServlet())
                .addRoute("/general-page", ConnectAppServlets.openDialogServlet(ADDON_WEBITEM_DIALOG))
                .addRoute("/my-webitem-dialog", ConnectAppServlets.closeDialogServlet())
                .addRoute("/rpd", ConnectAppServlets.dialogServlet())
                .addRoute("/fsd", ConnectAppServlets.sizeToParentServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    /**
     * Tests opening a dialog by key from a general page with json descriptor
     */

    @Test
    public void testOpenCloseDialogKeyWithPrependedAddOnKey() throws Exception
    {
        testOpenAndCloseWithPrependedAddOnKey(ADDON_GENERALPAGE, ADDON_DIALOG);
    }

    @Test
    public void testOpenCloseDialogKey()
    {
        testOpenAndClose(ADDON_GENERALPAGE, ADDON_DIALOG);
    }

    @Test
    public void testWebItemDialogOpenByKeyWithPrependedAddOnKey() throws Exception
    {
        testOpenAndCloseWithPrependedAddOnKey(ADDON_GENERALPAGE_WEBITEM_DIALOG, ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testWebItemDialogOpenByKey() throws Exception
    {
        testOpenAndClose(ADDON_GENERALPAGE_WEBITEM_DIALOG, ADDON_WEBITEM_DIALOG);
    }

    private void testOpenAndCloseWithPrependedAddOnKey(String pageKey, String dialogKey)
    {
        testOpenAndClose(pageKey, ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), dialogKey));
    }

    private void testOpenAndClose(String pageKey, String moduleKey)
    {
        RemoteDialogOpeningPage dialogOpeningPage = loginAndVisit(testUserFactory.basicUser(),
                RemoteDialogOpeningPage.class, runner.getAddon().getKey(), pageKey);
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey(moduleKey);

        assertThatTheDialogHasTheCorrectProperties(closeDialogPage);
        assertEquals("test dialog close data", closeTheDialog(dialogOpeningPage, closeDialogPage));
    }

    private String closeTheDialog(RemoteDialogOpeningPage dialogOpeningPage, RemoteCloseDialogPage closeDialogPage)
    {
        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        return dialogOpeningPage.waitForValue("dialog-close-data");
    }

    private void assertThatTheDialogHasTheCorrectProperties(RemoteCloseDialogPage closeDialogPage)
    {
        // check the dimensions are the same as those in the js (mustache file)
        assertThat(closeDialogPage.getIFrameSize().getWidth(), is(231));
        assertThat(closeDialogPage.getIFrameSize().getHeight(), is(356));
        assertTrue(closeDialogPage.getFromQueryString("ui-params").length() > 0);
        verifyIframeURLHasVersionNumber(closeDialogPage);
    }

    @Test
    public void testLoadGeneralDialog()
    {
        login(testUserFactory.basicUser());
        HomePage homePage = product.visit(HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, REMOTE_PLUGIN_DIALOG_KEY, runner.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertThat(remotePluginTest.getLocation(), endsWith(homePage.getUrl()));

        // Exercise the dialog's submit button.
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest);
        assertFalse(dialog.wasSubmitted());
        dialog.submitAndWaitUntilSubmitted();
        dialog.submitAndWaitUntilHidden();
    }

    @Test
    public void testSizeToParentDoesNotWorkInDialog()
    {
        login(testUserFactory.basicUser());
        product.visit(HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, SIZE_TO_PARENT_DIALOG_KEY, runner.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertTrue(remotePluginTest.isNotFullSize());
    }
}
