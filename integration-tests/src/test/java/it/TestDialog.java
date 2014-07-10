package it;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;

@XmlDescriptor
public class TestDialog extends ConnectWebDriverTestBase
{
    private static final String PAGE_MODULE_KEY = "remotePluginGeneralOpenDialog";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withKey(PAGE_MODULE_KEY)
                                .withName(new I18nProperty("Remotable Plugin app1 Open Dialog", null))
                                .withUrl("/rpg")
                                .build(),
                        newPageBean()
                                .withKey("my-dialog")
                                .withName(new I18nProperty("Remote dialog", null))
                                .withUrl("/my-dialog")
                                .withLocation("")
                                .build())
                .addRoute("/rpg", ConnectAppServlets.openDialogServlet())
                .addRoute("/my-dialog", ConnectAppServlets.closeDialogServlet())
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

    @Test
    public void testOpenCloseDialogKey() throws Exception
    {
        RemoteDialogOpeningPage dialogOpeningPage = getPageWithDialogLink();
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey("my-dialog");

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        String response = dialogOpeningPage.waitForValue("dialog-close-data");
        assertEquals("test dialog close data", response);
    }

    @Test
    public void testOpenCloseDialogKeyDimensions() throws Exception
    {
        RemoteCloseDialogPage closeDialogPage = getPageWithDialogLink().openKey("my-dialog");

        try
        {
            // check the dimensions are the same as those in the js (moustache file)
            assertThat(closeDialogPage.getIFrameSize(), both(hasProperty("width", is(231))).and(hasProperty("height", is(356))));
        }
        finally
        {
            closeDialogPage.close();
            closeDialogPage.waitUntilClosed();
        }
    }

    private RemoteDialogOpeningPage getPageWithDialogLink()
    {
        loginAsBetty();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, PAGE_MODULE_KEY, "Remotable Plugin app1 Open Dialog", remotePlugin.getAddon().getKey());

        page.clickAddOnLink();

        return product.getPageBinder().bind(RemoteDialogOpeningPage.class, null, AddonTestUtils.escapedAddonAndModuleKey(remotePlugin.getAddon().getKey(), PAGE_MODULE_KEY), remotePlugin.getAddon().getKey(), false);
    }
}
