package it.jira;

import com.atlassian.jira.pageobjects.pages.AddDashboardPage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.com.atlassian.gadgets.pages.AddGadgetDialog;
import it.com.atlassian.gadgets.pages.Gadget;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestDashboardItem extends JiraWebDriverTestBase
{
    private static final String ADDON_KEY = AddonTestUtils.randomAddOnKey();
    private static final String DASHBOARD_ITEM_TITLE = "Dashboard item title";
    private static final String DASHBOARD_ITEM_DESCRIPTION = "Dashboard item description";
    private static final TestUser TEST_USER = TestUser.BARNEY;
    private static final String VENDOR_NAME = "Atlassian";
    private static ConnectRunner addon;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        logout();

        addon = new ConnectRunner(product, ADDON_KEY)
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName(VENDOR_NAME).withUrl("http://www.atlassian.com").build())
                .addModules("jiraDashboardItems",
                        DashboardItemModuleBean.newBuilder()
                                .withDescription(new I18nProperty(DASHBOARD_ITEM_DESCRIPTION, "description.i18n.key"))
                                .withName(new I18nProperty(DASHBOARD_ITEM_TITLE, "description.title.key"))
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
    public void testAddingConnectDashboardItemToDashboard()
    {
        final DashboardPage dashboard = createDashboard(generateDashboardTitle());

        dashboard.gadgets()
                .openAddGadgetDialog()
                .addGadget(DASHBOARD_ITEM_TITLE)
                .simpleClose();

        final Gadget gadgetByTitle = product.getPageBinder().bind(DashboardPage.class)
                .gadgets()
                .getByTitle(DASHBOARD_ITEM_TITLE);

        assertNotNull("Dashboard contains item with title", gadgetByTitle);
    }

    @Test
    public void testConnectDashboardItemVisibleInDirectory()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        AddGadgetDialog gadgetDialog = dashboard.gadgets()
                .openAddGadgetDialog()
                .loadAllVisibleGadgets();

        assertThat("Dashboard item visible in directory", gadgetDialog.isGadgetVisible(DASHBOARD_ITEM_TITLE), is(true));
    }

    @Test
    public void testConnectDashboardHasProperDescription()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        AddGadgetDialog gadgetDialog = dashboard.gadgets()
                .openAddGadgetDialog()
                .loadAllVisibleGadgets();

        assertThat(gadgetDialog.getGadgetDescription(DASHBOARD_ITEM_TITLE), is(DASHBOARD_ITEM_DESCRIPTION));
    }

    @Test
    public void testConnectDashboardHasProperVendorInformation()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        AddGadgetDialog gadgetDialog = dashboard.gadgets()
                .openAddGadgetDialog()
                .loadAllVisibleGadgets();

        assertThat(gadgetDialog.getGadgetAuthor(DASHBOARD_ITEM_TITLE), is(VENDOR_NAME));
    }

    private String generateDashboardTitle() {return RandomStringUtils.random(10);}

    private DashboardPage createDashboard(final String dashboardName)
    {
        final AddDashboardPage addDashboardPage = loginAndVisit(TEST_USER, AddDashboardPage.class);
        addDashboardPage.setName(dashboardName);

        addDashboardPage.submit();

        return product.visit(DashboardPage.class);
    }
}
