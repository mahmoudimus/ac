package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static it.TestConstants.BETTY_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestDialog extends ConnectWebDriverTestBase
{
    public static final String EXTRA_PREFIX = "servlet-";
    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(GeneralPageModule.key("remotePluginGeneralOpenDialog")
                                      .name("Remotable Plugin app1 Open Dialog")
                                      .path("/rpg")
                                      .linkName("Remotable Plugin app1 Open Dialog")
                                      .resource(ConnectAppServlets.openDialogServlet()))
                .add(DialogPageModule.key("my-dialog")
                                      .name("Remote dialog")
                                      .path("/my-dialog")
                                      .section("")
                                      .resource(ConnectAppServlets.closeDialogServlet()))
                .addRoute("/dialog", ConnectAppServlets.closeDialogServlet())
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
     * Tests deprecated functionality; should be removed when ContextFreeIframePageServlet is deleted
     */
    @Test
    public void testOpenCloseDialogUrl()
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog", EXTRA_PREFIX);

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "servlet", "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openUrl();

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        String response = dialogOpeningPage.waitForValue("dialog-close-data");
        assertEquals("test dialog close data", response);
    }

    @Test
    public void testOpenCloseDialogKey() throws Exception
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog", EXTRA_PREFIX);

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "servlet", "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey("my-dialog");

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        String response = dialogOpeningPage.waitForValue("dialog-close-data");
        assertEquals("test dialog close data", response);
    }

    @Test
    public void testOpenCloseDialogKeyDimensions() throws Exception
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog", EXTRA_PREFIX);

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "servlet", "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey("my-dialog");

        assertNotEquals("test dialog width is not 0", closeDialogPage.getWidth(), "0");
        assertNotEquals("test dialog height is not 0", closeDialogPage.getHeight(), "0");
    }


    @Test
    public void testOpenCloseDialogUrlDimensions() throws Exception
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog", EXTRA_PREFIX);

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "servlet", "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openUrl();

        assertNotEquals("test dialog width is not 0", closeDialogPage.getWidth(), "0");
        assertNotEquals("test dialog height is not 0", closeDialogPage.getHeight(), "0");
    }

}
