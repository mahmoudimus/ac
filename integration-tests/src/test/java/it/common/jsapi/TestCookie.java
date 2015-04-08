package it.common.jsapi;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCookieGeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestCookie extends MultiProductWebDriverTestBase
{
    private static final String ADDON_GENERALPAGE = "ac-general-cookie-page";
    private static final String ADDON_GENERALPAGE_NAME = "AC Cookie Page";

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
                .addRoute("/pg", ConnectAppServlets.cookieServlet())
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
     * Tests setting a cookie
     */

    @Test
    public void testCreateCookie() throws Exception
    {
        loginAndVisit(ConnectTestUserFactory.basicUser(product), HomePage.class);
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class, ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME, remotePlugin.getAddon().getKey());
        remotePage.clickAddOnLink();
        RemoteCookieGeneralPage remoteCookiePage = product.getPageBinder().bind(RemoteCookieGeneralPage.class, ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), ADDON_GENERALPAGE));
        remoteCookiePage.readCookie();
        assertEquals(remoteCookiePage.getCookieContents(), "undefined");
        remoteCookiePage.saveCookie();
        remoteCookiePage.readCookie();
        assertEquals(remoteCookiePage.getCookieContents(), "cookie contents");
    }


    /**
     * Tests deleting a cookie
     */
    @Test
    public void testEraseCookie() throws Exception
    {
        loginAndVisit(ConnectTestUserFactory.basicUser(product), HomePage.class);
        GeneralPage remotePage = product.getPageBinder().bind(GeneralPage.class, ADDON_GENERALPAGE, ADDON_GENERALPAGE_NAME, remotePlugin.getAddon().getKey());
        remotePage.clickAddOnLink();
        RemoteCookieGeneralPage remoteCookiePage = product.getPageBinder().bind(RemoteCookieGeneralPage.class, ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), ADDON_GENERALPAGE));
        remoteCookiePage.saveCookie();
        remoteCookiePage.eraseCookie();
        remoteCookiePage.readCookie();
        assertEquals(remoteCookiePage.getCookieContents(), "undefined");
    }
}
