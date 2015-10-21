package it.common.jsapi;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.modules.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDialog extends MultiProductWebDriverTestBase
{

    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_GENERALPAGE_WEBITEM_DIALOG = "general-page-opening-webitem-dialog";
    private static final String ADDON_WEBITEM_DIALOG = "my-webitem-dialog";
    private static final String ADDON_FULL_PAGE_DIALOG = "full-page-dialog";
    private static final String ADDON_GENERALPAGE_WEBITEM_MULTIPLE_DIALOGS = "general-page-opening-webitem-multiple-dialogs";

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
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty("DD", null))
                                .withUrl("/general-page-creates-two-dialogs")
                                .withKey(ADDON_GENERALPAGE_WEBITEM_MULTIPLE_DIALOGS)
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty("EE", null))
                                .withUrl("/full-page-dialog")
                                .withKey(ADDON_FULL_PAGE_DIALOG)
                                .build()
                )
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("FF", null))
                                .withUrl("/my-webitem-dialog?myuserid={user.id}")
                                .withKey(ADDON_WEBITEM_DIALOG)
                                .withLocation("none")
                                .withContext(AddOnUrlContext.addon)
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .build()
                )
                .addRoute("/pg", ConnectAppServlets.openDialogServlet())
                .addRoute("/my-dialog-url", ConnectAppServlets.closeDialogServlet())
                .addRoute("/general-page", ConnectAppServlets.openDialogServlet(ADDON_WEBITEM_DIALOG))
                .addRoute("/my-webitem-dialog", ConnectAppServlets.closeDialogServlet())
                .addRoute("/general-page-creates-two-dialogs", ConnectAppServlets.openMultipleDialogsServlet())
                .addRoute("/full-page-dialog", ConnectAppServlets.closeDialogServlet())
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

    @Test
    public void testCanCreateCustomButtonInDialog()
    {
        // the confluence page
        RemoteDialogOpeningPage pageToOpenDialogFrom = loginAndVisit(testUserFactory.basicUser(),
                RemoteDialogOpeningPage.class, runner.getAddon().getKey(), ADDON_GENERALPAGE_WEBITEM_DIALOG);

        // open a dialog and bind
        RemoteCloseDialogPage theOpenedDialog = pageToOpenDialogFrom.clickToOpenDialog("dialog-open-button-for-custom-button-dialog", ADDON_WEBITEM_DIALOG);

        // get the custom button to open another dialog
        PageElement button = pageToOpenDialogFrom.getButtonByClassName("ap-dialog-custom-button");

        assertEquals("open full page dialog", button.getText());
        assertEquals("test dialog close data", closeTheDialog(pageToOpenDialogFrom, theOpenedDialog));
    }

    @Test
    public void testCanCreateMultipleDialogs()
    {
        // the confluence page
        RemoteDialogOpeningPage pageToOpenDialogFrom = loginAndVisit(testUserFactory.basicUser(),
                RemoteDialogOpeningPage.class, runner.getAddon().getKey(), ADDON_GENERALPAGE_WEBITEM_MULTIPLE_DIALOGS);

        // open a dialog which will open a second dialog. We test for the top one
        RemoteCloseDialogPage theOpenedDialog = pageToOpenDialogFrom.clickToOpenDialog("dialog-open-button-for-multiple-dialogs", ADDON_FULL_PAGE_DIALOG);

        assertThat(theOpenedDialog.getIFrameSize().getWidth(), is(800));
        assertThat(theOpenedDialog.getIFrameSize().getHeight(), is(400));
        assertEquals("test dialog close data", closeTheDialog(pageToOpenDialogFrom, theOpenedDialog));

        // maybe check the one below was closed?
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
}
