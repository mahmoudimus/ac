package it.confluence.jsapi;

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
        loginAndClickToNavigate("navigate-to-dashboard");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertThat(relativeUrl, anyOf(is("/confluence"), is("/#all-updates"), is("/#recently-viewed")));

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
        assertEquals("/pages/editpage.action?pageId=98311", relativeUrl);

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

    @Test
    public void testNavigateToSpace() throws Exception
    {
        loginAndClickToNavigate("navigate-to-space");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/display/DS", relativeUrl);

        makeSureNo404s();
    }

    @Test
    public void testNavigateToSpaceTools() throws Exception
    {
        loginAndClickToNavigate("navigate-to-space-tools");

        String relativeUrl = getRelativeUrlFromWebDriver();
        assertEquals("/spaces/viewspacesummary.action?key=DS", relativeUrl);

        makeSureNo404s();
    }

    public void loginAndClickToNavigate(String id)
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        page.open(id);
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
