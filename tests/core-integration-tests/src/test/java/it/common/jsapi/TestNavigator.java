package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectGeneralTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteNavigatorGeneralPage;
import it.common.MultiProductWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;

public class TestNavigator extends MultiProductWebDriverTestBase
{
    private static final String PAGE_KEY = "ac-navigator-general-page";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Nvg", null))
                                .withUrl("/nvg")
                                .withKey(PAGE_KEY)
                                .withLocation(getGloballyVisibleLocation())
                                .build()
                )
                .addRoute("/nvg", ConnectAppServlets.navigatorServlet())
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
    public void testNavigateToDashboard() throws Exception
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        ConnectGeneralTestPage dashboard = page.clickToNavigate("navigate-to-dashboard");

        // test the navigation happened

    }
}
