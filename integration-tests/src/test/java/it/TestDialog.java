package it;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@XmlDescriptor
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
        loginAsBetty();
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
        loginAsBetty();
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
        loginAsBetty();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog", EXTRA_PREFIX);

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "servlet", "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey("my-dialog");

        // check the dimensions are the same as those in the js (moustache file)
        assertThat(closeDialogPage.getIFrameSize().getWidth(), is(231));
        assertThat(closeDialogPage.getIFrameSize().getHeight(), is(356));

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
    }


    @Test
    public void testOpenCloseDialogUrlDimensions() throws Exception
    {
        loginAsBetty();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneralOpenDialog", "Remotable Plugin app1 Open Dialog", EXTRA_PREFIX);

        page.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "servlet", "remotePluginGeneralOpenDialog", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openUrl();

        // check the dimensions are the same as those in the js (moustache file)
        assertThat(closeDialogPage.getIFrameSize().getWidth(), is(654));
        assertThat(closeDialogPage.getIFrameSize().getHeight(), is(918));

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
    }

}
