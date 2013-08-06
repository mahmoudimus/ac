package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.pageobjects.GeneralPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.GeneralPageModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static it.TestConstants.BETTY;

public class TestDialog extends AbstractRemotablePluginTest
{
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
                        .resource(newMustacheServlet("iframe-open-dialog.mu")))
                .addRoute("/dialog", newMustacheServlet("iframe-close-dialog.mu"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testOpenCloseDialog()
    {
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog");

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.open();

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
    }
}
