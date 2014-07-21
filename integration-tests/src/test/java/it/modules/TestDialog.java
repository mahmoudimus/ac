package it.modules;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.*;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.util.TestConstants.BETTY_USERNAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class TestDialog extends ConnectWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC General Page";

    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_DIALOG_NAME = "my dialog";

    private static final String ADDON_GENERALPAGE_WEBITEM_DIALOG = "general-page-opening-webitem-dialog";
    private static final String ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG = "WebItem Dialog Opener Page";

    private static final String ADDON_WEBITEM_DIALOG = "my-webitem-dialog";
    private static final String ADDON_WEBITEM_DIALOG_NAME = "my webitem dialog";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String productContextPath = product.getProductInstance().getContextPath().toLowerCase();
        String globallyVisibleLocation = productContextPath.contains("jira")
                ? "system.top.navigation.bar"
                : productContextPath.contains("wiki") || productContextPath.contains("confluence")
                ? "system.help/pages"
                : null;

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME, null))
                                .withUrl("/pg")
                                .withKey(ADDON_GENERALPAGE)
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_DIALOG_NAME, null))
                                .withUrl("/my-dialog-url?myuserid={user.id}")
                                .withKey(ADDON_DIALOG)
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, null))
                                .withUrl("/general-page")
                                .withKey(ADDON_GENERALPAGE_WEBITEM_DIALOG)
                                .build()
                )
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(ADDON_WEBITEM_DIALOG_NAME, null))
                                .withUrl("/my-webitem-dialog?myuserid={user.id}")
                                .withKey(ADDON_WEBITEM_DIALOG)
                                .withLocation("none")
                                .withContext(AddOnUrlContext.addon)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .build(),
                        newWebItemBean()
                                .withKey("remotePluginDialog")
                                .withName(new I18nProperty("Remotable Plugin app1 Dialog", null))
                                .withUrl("/rpd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newWebItemBean()
                                .withKey("sizeToParentDialog")
                                .withName(new I18nProperty("Size to parent dialog page", null))
                                .withUrl("/fsd")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(globallyVisibleLocation)
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
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    /**
     * Tests opening a dialog by key from a general page with json descriptor
     */

    @Test
    public void testOpenCloseDialogKeyWithPrependedAddOnKey() throws Exception
    {
        testOpenAndCloseWithPrependedAddOnKey(ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME, ADDON_DIALOG);
    }

    @Test
    public void testOpenCloseDialogKey()
    {
        testOpenAndClose(ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME, ADDON_DIALOG);
    }

    @Test
    public void testWebItemDialogOpenByKeyWithPrependedAddOnKey() throws Exception
    {
        testOpenAndCloseWithPrependedAddOnKey(ADDON_GENERALPAGE_WEBITEM_DIALOG, ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, ADDON_WEBITEM_DIALOG);
    }

    @Test
    public void testWebItemDialogOpenByKey() throws Exception
    {
        testOpenAndClose(ADDON_GENERALPAGE_WEBITEM_DIALOG, ADDON_GENERALPAGE_NAME_WEBITEM_DIALOG, ADDON_WEBITEM_DIALOG);
    }

    private void testOpenAndCloseWithPrependedAddOnKey(String pageKey, String pageName, String dialogKey)
    {
        testOpenAndClose(pageKey, pageName, AddonTestUtils.escapedAddonAndModuleKey(remotePlugin.getAddon().getKey(), dialogKey));
    }

    private void testOpenAndClose(String pageKey, String pageName, String moduleKey)
    {
        loginAsAdmin();
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class, pageKey, pageName, remotePlugin.getAddon().getKey());
        remotePage.clickAddOnLink();

        RemoteDialogOpeningPage dialogOpeningPage = bindDialogOpeningPage(AddonTestUtils.escapedAddonAndModuleKey(remotePlugin.getAddon().getKey(), pageKey));
        RemoteCloseDialogPage closeDialogPage = bindCloseDialogPage(dialogOpeningPage, moduleKey);

        assertThatTheDialogHasTheCorrectProperties(closeDialogPage);
        assertEquals("test dialog close data", closeTheDialog(dialogOpeningPage, closeDialogPage));
    }

    private RemoteCloseDialogPage bindCloseDialogPage(RemoteDialogOpeningPage dialogOpeningPage, String moduleKey)
    {
        return dialogOpeningPage.openKey(moduleKey);
    }

    private RemoteDialogOpeningPage bindDialogOpeningPage(String moduleKey)
    {
        return product.getPageBinder().bind(RemoteDialogOpeningPage.class, null, moduleKey, remotePlugin.getAddon().getKey());
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
        assertThat(closeDialogPage.getFromQueryString("user_id"), is("admin"));
    }

    @Test
    public void testLoadGeneralDialog()
    {
        loginAsBetty();

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginDialog", "Remotable Plugin app1 Dialog", remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertNotNull(remotePluginTest.getFullName());
        Assert.assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(BETTY_USERNAME));

        // Exercise the dialog's submit button.
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest);
        assertFalse(dialog.wasSubmitted());
        assertEquals(false, dialog.submit());

        assertTrue(dialog.wasSubmitted());
        assertEquals(true, dialog.submit());
    }

    @Test
    public void testSizeToParentDoesNotWorkInDialog()
    {
        loginAsBetty();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "sizeToParentDialog", "Size to parent dialog page", remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertTrue(remotePluginTest.isNotFullSize());
    }
}
