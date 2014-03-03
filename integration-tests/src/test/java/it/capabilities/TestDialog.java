package it.capabilities;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDialog extends ConnectWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC General Page";

    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_DIALOG_NAME = "my dialog";

    private static final String ADDON_WEBITEM_DIALOG = "my-webitem-dialog";
    private static final String ADDON_WEBITEM_DIALOG_NAME = "my webitem dialog";

    private static final String SPACE = "ds";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME, null))
                                .withUrl("/pg?page_id{page.id}")
                                .withKey(ADDON_GENERALPAGE)
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_DIALOG_NAME, null))
                                .withUrl("/my-dialog-url?page_id{page.id}&myuserid={user.id}")
                                .withKey(ADDON_DIALOG)
                                .build(),
                        newPageBean()
                                .withName(new I18nProperty(ADDON_WEBITEM_DIALOG_NAME, null))
                                .withUrl("/my-webitem-dialog")
                                .withKey(ADDON_WEBITEM_DIALOG)
                        .build()
                )

                .addRoute("/pg", ConnectAppServlets.openDialogServlet())
                .addRoute("/my-dialog-url", ConnectAppServlets.closeDialogServlet())
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
    public void testOpenCloseDialogKey() throws Exception
    {
        loginAsAdmin();
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class,ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME);
        remotePage.clickRemotePluginLink();

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, null, ADDON_GENERALPAGE, remotePlugin.getAddon().getKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey(ADDON_DIALOG);

        // check the dimensions are the same as those in the js (moustache file)
        assertThat(closeDialogPage.getIFrameSize().getWidth(), is(231));
        assertThat(closeDialogPage.getIFrameSize().getHeight(), is(356));
        assertTrue(closeDialogPage.getFromQueryString("ui-params").length() > 0);

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        String response = dialogOpeningPage.waitForValue("dialog-close-data");
        assertEquals("test dialog close data", response);

    }

}
