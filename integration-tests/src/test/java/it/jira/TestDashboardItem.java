package it.jira;

import com.atlassian.jira.pageobjects.gadgets.GadgetContainer;
import com.atlassian.jira.pageobjects.pages.AddDashboardPage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.projects.pageobjects.webdriver.page.ReportsPage;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDashboardItem extends JiraWebDriverTestBase
{
    private static final String ADDON_KEY = AddonTestUtils.randomAddOnKey();
    private static final TestUser TEST_USER = TestUser.BARNEY;
    private static ConnectRunner addon;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        logout();

        addon = new ConnectRunner(product, ADDON_KEY)
                .setAuthenticationToNone()
                .addModules("jiraDashboardItems",
                        DashboardItemModuleBean.newBuilder()
                            .withDescription(new I18nProperty("Dashboard item description", "description.i18n.key"))
                            .withTitle(new I18nProperty("Dashboard item title", "description.title.key"))
                            .withThumbnailUrl("atlassian-icon-16.png")
                            .withUrl("/dashboard-item-test")
                            .withKey("dashboard-item-key")
                            .build())
                .addRoute("/dashboard-item-test", ConnectAppServlets.apRequestServlet())
                .start();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void testConnectDashboardItemVisibleInDirectory()
    {
        createDashboard(RandomStringUtils.random(10));

        DashboardPage dashboardPage = product.visit(DashboardPage.class);
        GadgetContainer gadgets = dashboardPage.gadgets();

    }

    private void createDashboard(final String dashboardName)
    {
        final AddDashboardPage addDashboardPage = loginAndVisit(TEST_USER, AddDashboardPage.class);
        addDashboardPage.setName(dashboardName);

        addDashboardPage.submit();
    }
}
