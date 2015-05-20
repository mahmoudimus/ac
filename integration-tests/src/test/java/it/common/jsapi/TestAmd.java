package it.common.jsapi;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAmd extends MultiProductWebDriverTestBase
{

    public static final String PAGE_NAME = "AMD";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey("amdTest")
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withUrl("/amdTest")
                                .withLocation(getGloballyVisibleLocation())
                                .build())
                .addRoute("/amdTest", ConnectAppServlets.amdTestServlet())
                .addScope(ScopeName.READ)
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
    public void testAmd()
    {
        loginAndVisit(testUserFactory.admin(), HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "amdTest", PAGE_NAME, remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertEquals("true", remotePluginTest.waitForValue("amd-env"));
        assertEquals("true", remotePluginTest.waitForValue("amd-request"));
        assertEquals("true", remotePluginTest.waitForValue("amd-dialog"));
    }
}
