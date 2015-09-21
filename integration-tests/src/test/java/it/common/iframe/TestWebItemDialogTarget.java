package it.common.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestWebItemDialogTarget extends MultiProductWebDriverTestBase
{

    private static final String REMOTE_PLUGIN_DIALOG_KEY = "remotePluginDialog";
    private static final String SIZE_TO_PARENT_DIALOG_KEY = "sizeToParentDialog";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        logout();

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
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
                                .build()
                )
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
