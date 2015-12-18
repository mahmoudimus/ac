package it.confluence.jsapi;

import com.atlassian.confluence.webdriver.pageobjects.page.SimpleDashboardPage;
import com.atlassian.confluence.webdriver.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.webdriver.pageobjects.page.DashboardPage;
import com.atlassian.confluence.webdriver.pageobjects.page.content.EditContentPage;
import com.atlassian.confluence.webdriver.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.confluence.webdriver.pageobjects.page.user.ViewProfilePage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceUserProfilePage;
import com.atlassian.connect.test.confluence.pageobjects.RemoteNavigatorGeneralPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.servlet.ConfluenceAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class TestNavigator extends ConfluenceWebDriverTestBase
{
    private static final String PAGE_KEY = "ac-navigator-general-page";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(ConfluenceWebDriverTestBase.product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Nvg", null))
                                .withUrl("/nvg")
                                .withKey(PAGE_KEY)
                                .withLocation("system.header/left")
                                .build()
                )
                .addRoute("/nvg", ConfluenceAppServlets.navigatorServlet())
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
        SimpleDashboardPage dashboard = loginAndClickToNavigate("navigate-to-dashboard", SimpleDashboardPage.class);

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertThat(relativeUrl, anyOf(is("/confluence"), is("/#all-updates"), is("/#recently-viewed")));

        makeSureNo404s();
    }

    @Test
    public void testNavigateToPage() throws Exception
    {
        ViewPage viewpage = loginAndClickToNavigate("navigate-to-page", ViewPage.class);

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/pages/viewpage.action?pageId=98311", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToEditPage() throws Exception
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        page.open("navigate-to-edit-page");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/pages/editpage.action?pageId=98311", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToUserProfile() throws Exception
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        page.open("navigate-to-user-profile");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/display/~admin", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToSpaceTools() throws Exception
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        page.open("navigate-to-space-tools");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/spaces/viewspacesummary.action?key=DS", relativeUrl);

        makeSureNo404s();
    }

    public <P extends com.atlassian.pageobjects.Page> P loginAndClickToNavigate(String id, java.lang.Class<P> aPageClass)
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        return page.clickToNavigate(id, aPageClass);
    }

    public void makeSureNo404s()
    {
        String pageSource = ConfluenceWebDriverTestBase.product.getTester().getDriver().getPageSource();
        assertFalse(pageSource.contains("Page Not Found"));
    }

    /*
     Here we ask webdriver for the absolute url, and we ask the product for its base url.
     Using both of them, we can be confident of what the relative url actually is, to test with.
      */
    public String getRelativeUrlFromWebDriver()
    {
        String absoluteUrl = ConfluenceWebDriverTestBase.product.getTester().getDriver().getCurrentUrl();
        String baseUrl = ConfluenceWebDriverTestBase.product.getProductInstance().getBaseUrl();
        return absoluteUrl.replace(baseUrl, "");
    }
}
