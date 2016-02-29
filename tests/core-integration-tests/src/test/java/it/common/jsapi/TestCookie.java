package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCookieGeneralPage;
import it.common.MultiProductWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestCookie extends MultiProductWebDriverTestBase {
    private static final String PAGE_KEY = "ac-general-cookie-page";

    private static ConnectRunner remotePlugin;


    @BeforeClass
    public static void startConnectAddon() throws Exception {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
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
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    /**
     * Tests setting a cookie
     */

    @Test
    public void testCreateCookie() throws Exception {
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
    public void testEraseCookie() throws Exception {
        RemoteCookieGeneralPage remoteCookiePage = loginAndVisit(testUserFactory.basicUser(),
                RemoteCookieGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        remoteCookiePage.saveCookie();
        remoteCookiePage.eraseCookie();
        remoteCookiePage.readCookie();
        assertEquals(remoteCookiePage.getCookieContents(), "undefined");
    }
}
