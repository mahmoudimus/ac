package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCookieGeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestCookie extends MultiProductWebDriverTestBase
{
    private static final String PAGE_KEY = "ac-general-cookie-page";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Cookie", null))
                                .withLocation(getGloballyVisibleLocation())
                                .withUrl("/pg")
                                .withKey(PAGE_KEY)
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
        RemoteCookieGeneralPage remoteCookiePage = loginAndVisit(testUserFactory.basicUser(),
                RemoteCookieGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

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
        RemoteCookieGeneralPage remoteCookiePage = loginAndVisit(testUserFactory.basicUser(),
                RemoteCookieGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        remoteCookiePage.saveCookie();
        remoteCookiePage.eraseCookie();
        remoteCookiePage.readCookie();
        assertEquals(remoteCookiePage.getCookieContents(), "undefined");
    }
}
