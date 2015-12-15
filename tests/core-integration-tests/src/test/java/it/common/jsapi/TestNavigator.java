package it.common.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteNavigatorGeneralPage;
import it.common.MultiProductWebDriverTestBase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    /*
     If you've been messing around locally on the instance it may redirect to the dashboard
     being at #recently-viewed or #all-updates depending on what you've clicked. In this case
     the test isn't broken, you just need a fresh startup.
     */
    @Test
    public void testNavigateToDashboard() throws Exception
    {
        loginAndClickToNavigate("navigate-to-dashboard");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/confluence", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToPage() throws Exception
    {
        loginAndClickToNavigate("navigate-to-page");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/pages/viewpage.action?pageId=98311", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToEditPage() throws Exception
    {
        loginAndClickToNavigate("navigate-to-edit-page");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/editpage.action?pageId=98311", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToUserProfile() throws Exception
    {
        loginAndClickToNavigate("navigate-to-user-profile");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/display/~admin", relativeUrl);

        makeSureNo404s();
    }

    public void loginAndClickToNavigate(String id)
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        page.open(id);
    }

    public void makeSureNo404s()
    {
        String pageSource = product.getTester().getDriver().getPageSource();
        assertFalse(pageSource.contains("Page Not Found"));
    }

    /*
     Here we ask webdriver for the absolute url, and we ask the product for its base url.
     Using both of them, we can be confident of what the relative url actually is, to test with.
      */
    public String getRelativeUrlFromWebDriver()
    {
        String absoluteUrl = product.getTester().getDriver().getCurrentUrl();
        String baseUrl = product.getProductInstance().getBaseUrl();
        return absoluteUrl.replace(baseUrl, "");
    }
}
