package it.common.jsapi;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteMessageGeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestMessage extends MultiProductWebDriverTestBase
{
    private static final String PAGE_KEY = "ac-message-general-page";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Msg", null))
                                .withUrl("/pg")
                                .withKey(PAGE_KEY)
                                .withLocation(getGloballyVisibleLocation())
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
        loginAndVisit(testUserFactory.basicUser(), HomePage.class);
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class, PAGE_KEY, remotePlugin.getAddon().getKey());
        remotePage.clickAddOnLink();
        RemoteMessageGeneralPage remoteMessagePage = product.getPageBinder().bind(RemoteMessageGeneralPage.class, ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), PAGE_KEY));
        remoteMessagePage.openInfoMessage();
        assertEquals(remoteMessagePage.getMessageTitleText(), "plain text title");
    }
}
