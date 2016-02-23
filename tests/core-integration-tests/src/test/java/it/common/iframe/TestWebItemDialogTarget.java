package it.common.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.common.MultiProductWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestWebItemDialogTarget extends MultiProductWebDriverTestBase {

    private static final String REMOTE_PLUGIN_DIALOG_KEY = "remotePluginDialog";
    private static final String SIZE_TO_PARENT_DIALOG_KEY = "sizeToParentDialog";
    private static final String MULTIPLE_DIALOG_1_DIALOG_KEY = "multipleDialogs1Dialog";
    private static final String MULTIPLE_DIALOG_2_DIALOG_KEY = "multipleDialogs2Dialog";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .addJWT()
                .addModules("webItems",
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
                                .build(),
                        newWebItemBean()
                                .withKey(MULTIPLE_DIALOG_1_DIALOG_KEY)
                                .withName(new I18nProperty("Multiple-dialog 1", null))
                                .withUrl("/mdd1")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newWebItemBean()
                                .withKey(MULTIPLE_DIALOG_2_DIALOG_KEY)
                                .withName(new I18nProperty("Multiple-dialog 2", null))
                                .withUrl("/mdd2")
                                .withTarget(newWebItemTargetBean()
                                        .withType(WebItemTargetType.dialog)
                                        .build())
                                .withLocation("not-shown")
                                .build()
                )
                .addRoute("/rpd", ConnectAppServlets.dialogServlet())
                .addRoute("/fsd", ConnectAppServlets.sizeToParentServlet())
                .addRoute("/mdd1", ConnectAppServlets.mustacheServlet("multiple-dialog-1.mu"))
                .addRoute("/mdd2", ConnectAppServlets.mustacheServlet("multiple-dialog-2.mu"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (runner != null) {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testLoadGeneralDialog() throws MalformedURLException {
        login(testUserFactory.basicUser());
        HomePage homePage = product.visit(HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, REMOTE_PLUGIN_DIALOG_KEY, runner.getAddon().getKey());
        ConnectAddonEmbeddedTestPage remotePluginTest = page.clickAddonLink();
        String location = remotePluginTest.getLocation();
        URL locationUrl = new URL(location);
        assertThat(locationUrl.getPath(), endsWith(homePage.getUrl()));

        // Exercise the dialog's submit button.
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest);
        assertFalse(dialog.wasSubmitted());
        dialog.submitAndWaitUntilSubmitted();
        dialog.submitAndWaitUntilHidden();
    }

    @Test
    public void testSizeToParentDoesNotWorkInDialog() {
        login(testUserFactory.basicUser());
        product.visit(HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, SIZE_TO_PARENT_DIALOG_KEY, runner.getAddon().getKey());
        ConnectAddonEmbeddedTestPage remotePluginTest = page.clickAddonLink();
        assertTrue(remotePluginTest.isNotFullSize());
    }

    @Test
    public void testMultipleDialogs() throws MalformedURLException {
        login(testUserFactory.basicUser());
        product.visit(HomePage.class);

        final String addonKey = runner.getAddon().getKey();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, MULTIPLE_DIALOG_1_DIALOG_KEY, addonKey);
        ConnectAddonEmbeddedTestPage dialog1Page = page.clickAddonLink();

        // The first dialog should have a button to launch a second dialog.
        RemoteLayeredDialog dialog1 = product.getPageBinder().bind(RemoteLayeredDialog.class, dialog1Page, true);
        assertThat(dialog1.getIFrameElementText("dialog-name"), is("Dialog1"));
        RemoteLayeredDialog dialog2 = launchSecondDialog(dialog1, addonKey);

        // When the second dialog is closed, the first dialog should be visible and retain its original content.
        dialog2.cancelAndWaitUntilHidden();
        assertThat(dialog1.getIFrameElementText("dialog-name"), is("Dialog1"));

        // Dialog 1 custom button binding should still work.
        dialog2 = launchSecondDialog(dialog1, addonKey);

        // ... and finally, both dialogs should tear down neatly.
        dialog2.cancelAndWaitUntilHidden();
        dialog1.cancelAndWaitUntilHidden();
    }

    private RemoteLayeredDialog launchSecondDialog(RemoteLayeredDialog dialog1, String addonKey) {
        dialog1.clickButtonWithClass("ap-dialog-custom-button");

        // The second dialog should be opened, and have the expected content.
        ConnectAddonEmbeddedTestPage dialog2Page = product.getPageBinder().bind(ConnectAddonEmbeddedTestPage.class, addonKey, MULTIPLE_DIALOG_2_DIALOG_KEY, true);
        RemoteLayeredDialog dialog2 = product.getPageBinder().bind(RemoteLayeredDialog.class, dialog2Page, false);
        assertThat(dialog2.getIFrameElementText("dialog-name"), is("Dialog2"));

        return dialog2;
    }
}
