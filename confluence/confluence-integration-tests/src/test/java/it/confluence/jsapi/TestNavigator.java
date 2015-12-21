package it.confluence.jsapi;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.api.model.longtasks.LongTaskStatus;
import com.atlassian.confluence.api.model.longtasks.LongTaskSubmission;
import com.atlassian.confluence.webdriver.pageobjects.page.SimpleDashboardPage;
import com.atlassian.confluence.webdriver.pageobjects.page.content.EditContentPage;
import com.atlassian.confluence.webdriver.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.webdriver.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.confluence.webdriver.pageobjects.page.user.ViewProfilePage;
import com.atlassian.connect.test.confluence.pageobjects.RemoteNavigatorGeneralPage;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.util.concurrent.Promise;
import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.servlet.ConfluenceAppServlets;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.fail;

public class TestNavigator extends ConfluenceWebDriverTestBase
{
    /*
     These tests work by navigating to a page and then attempting to bind a page object to that page.
     If the navigation itself fails, the page will not bind successfully and the test will fail.
     */

    private static List<Exception> setupFailure = new ArrayList<>();
    private static final String PAGE_KEY = "ac-navigator-general-page";
    private static ConnectRunner remotePlugin;

    private static Promise<Content> createdPage;
    private static Space space;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        try
        {
            String spaceKey = "NAVTEST";
            space = restClient.spaces().create(Space.builder().key(spaceKey).name("Nav Space").build(), false).get();
            createdPage = restClient.content().create(Content.builder(ContentType.PAGE)
                    .space(space)
                    .body("<p>Page content</p>", ContentRepresentation.STORAGE)
                    .title("Page")
                    .build());

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
                    .addRoute("/nvg", ConfluenceAppServlets.navigatorServlet(createdPage.get().getId().asLong()))
                    .start();
        }catch (Exception ex)
        {
            setupFailure.add(ex);
        }

    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        if (!setupFailure.isEmpty())
            throw setupFailure.get(0);
    }

    @After
    public void tearDown() throws Exception
    {
        Promise<LongTaskSubmission> task = restClient.spaces().delete(Space.builder().key(space.getKey()).build());

        // this should be moved into RemoteLongTaskService
        Option<LongTaskStatus> longTaskStatus = restClient.longTasks().get(task.get().getId()).get();

        final int waitTime = 50;
        final int retry = 100;
        for (int i = 0 ; longTaskStatus.get().getPercentageComplete() < 100; i++)
        {
            Thread.sleep(50); // wait for the space deletion to finish
            longTaskStatus = restClient.longTasks().get(task.get().getId()).get();
            if (i > 100)
                fail("Delete space long task has not yet completed after " + waitTime * retry);
        }
    }

    @Test
    public void testNavigateToDashboard() throws Exception
    {
        SimpleDashboardPage dashboard = loginAndClickToNavigate("navigate-to-dashboard", SimpleDashboardPage.class);
    }

    @Test
    public void testNavigateToPage() throws Exception
    {
        ViewPage viewpage = loginAndClickToNavigate("navigate-to-page", ViewPage.class);
    }

    @Test
    public void testNavigateToEditPage() throws Exception
    {
        EditContentPage editContentPage = loginAndClickToNavigate("navigate-to-edit-page", EditContentPage.class, Content.builder(ContentType.PAGE, createdPage.get().getId().asLong()).build());
    }

    @Test
    public void testNavigateToUserProfile() throws Exception
    {
        ViewProfilePage viewProfilePage = loginAndClickToNavigate("navigate-to-user-profile", ViewProfilePage.class, "admin");
    }

    @Test
    public void testNavigateToSpaceTools() throws Exception
    {
        String spaceKey = "DS";
        ViewSpaceSummaryPage viewSpaceSummaryPage = loginAndClickToNavigate("navigate-to-space-tools", ViewSpaceSummaryPage.class, Space.builder().key(spaceKey).build());
    }

    public <P extends com.atlassian.pageobjects.Page> P loginAndClickToNavigate(String id, java.lang.Class<P> aPageClass, Object...args)
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        return page.clickToNavigate(id, aPageClass, args);
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
