package it.capabilities;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteMessageGeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestMessage extends ConnectWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC General Page";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(ADDON_GENERALPAGE_NAME, null))
                                .withUrl("/pg")
                                .withKey(ADDON_GENERALPAGE)
                                .build()
                )
                .addRoute("/pg", ConnectAppServlets.openMessageServlet())
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
     * Tests opening an info message from a general page with json descriptor
     */

    @Test
    public void testCreateInfoMessage() throws Exception
    {
        loginAsAdmin();
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class, AddonTestUtils.escapedAddonAndModuleKey(remotePlugin.getAddon().getKey(), ADDON_GENERALPAGE), ADDON_GENERALPAGE_NAME);
        remotePage.clickRemotePluginLink();
        RemoteMessageGeneralPage remoteMessagePage = product.getPageBinder().bind(RemoteMessageGeneralPage.class, AddonTestUtils.escapedAddonAndModuleKey(remotePlugin.getAddon().getKey(), ADDON_GENERALPAGE));
        remoteMessagePage.openInfoMessage();
        assertEquals(remoteMessagePage.getMessageTitleText(), "plain text title");
    }
}