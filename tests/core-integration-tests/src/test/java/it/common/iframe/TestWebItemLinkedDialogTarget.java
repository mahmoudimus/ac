package it.common.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.DialogModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.common.MultiProductWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Dimension;

import static com.atlassian.plugin.connect.modules.beans.DialogModuleBean.newDialogBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions.newDialogOptions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestWebItemLinkedDialogTarget extends MultiProductWebDriverTestBase
{
    private static final String LINKED_DIALOG_KEY_WEBITEM = "linkedDialogWebItem";
    private static final String BADLY_LINKED_DIALOG_KEY_WEBITEM = "badlyLinkedDialogWebItem";
    private static final String LINKED_DIALOG_KEY = "linkedDialog";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        // This dialog and webItem pair are used in tests that a webItem target can be a common dialog module.
        DialogModuleBean linkedDialogDialog = newDialogBean()
                .withKey(LINKED_DIALOG_KEY)
                .withName(new I18nProperty("The Linked dialog", null))
                .withUrl("/ld")
                .withOptions(newDialogOptions().withWidth("456px").withHeight("567px").build())
                .build();
        WebItemModuleBean linkedDialogWebItem = newWebItemBean()
                .withKey(LINKED_DIALOG_KEY_WEBITEM)
                .withName(new I18nProperty("Links to dialog", null))
                .withTarget(newWebItemTargetBean()
                        .withType(WebItemTargetType.dialog)
                        .withKey(LINKED_DIALOG_KEY)   // Note! This target key matches the key of the dialog.
                        .build())
                .withLocation(getGloballyVisibleLocation())
                .build();
        WebItemModuleBean badlyLinkedDialogWebItem = newWebItemBean()
                .withKey(BADLY_LINKED_DIALOG_KEY_WEBITEM)
                .withName(new I18nProperty("Bad link", null))
                .withUrl("/ld")
                .withTarget(newWebItemTargetBean()
                        .withType(WebItemTargetType.dialog)
                        .withKey("i-am-wrong")
                        .build())
                .withLocation(getGloballyVisibleLocation())
                .build();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .addJWT()
                .addModules("webItems", linkedDialogWebItem, badlyLinkedDialogWebItem)
                .addModules("dialogs", linkedDialogDialog)
                .addRoute("/ld", ConnectAppServlets.mustacheServlet("linked-dialog.mu"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testLinkedDialog()
    {
        loginAndVisit(testUserFactory.basicUser(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, LINKED_DIALOG_KEY_WEBITEM, runner.getAddon().getKey());
        ConnectAddonEmbeddedTestPage dialogPage = page.clickAddonLink();
        RemoteLayeredDialog dialog = product.getPageBinder().bind(RemoteLayeredDialog.class, dialogPage, true);

        // Check that the dialog options are used by the webItem.
        Dimension size = dialog.getIFrameSize();
        assertThat(size.getWidth(), is(456));
        assertThat(size.getHeight(), is(567));

        // Check that the dialog url overrides the web-item one.
        assertThat(dialog.getIFrameElementText("dialog-name"), is("Linked Dialog"));
    }

    @Test
    public void testBadlyLinkedDialog()
    {
        loginAndVisit(testUserFactory.basicUser(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, BADLY_LINKED_DIALOG_KEY_WEBITEM, runner.getAddon().getKey());
        ConnectAddonEmbeddedTestPage dialogPage = page.clickAddonLink();
        RemoteLayeredDialog dialog = product.getPageBinder().bind(RemoteLayeredDialog.class, dialogPage, true);

        // Even though the dialog linked by the web-item's target.key is incorrect, the web-item should
        // launch a dialog.
        assertThat(dialog.getIFrameElementText("dialog-name"), is("Linked Dialog"));
    }
}
