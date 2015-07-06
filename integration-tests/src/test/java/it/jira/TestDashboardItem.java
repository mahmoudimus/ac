package it.jira;

import com.atlassian.jira.pageobjects.gadgets.GadgetContainer;
import com.atlassian.jira.pageobjects.pages.AddDashboardPage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.gadgets.pages.AddGadgetDialog;
import it.com.atlassian.gadgets.pages.Gadget;
import it.com.atlassian.gadgets.pages.GadgetMenu;
import it.servlet.ConnectAppServlets;
import it.servlet.TestServletContextExtractor;
import it.servlet.condition.DashboardItemConditionServlet;
import it.util.TestUser;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.Callable;
import javax.inject.Inject;

import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestDashboardItem extends JiraWebDriverTestBase
{
    private static final String ADDON_KEY = AddonTestUtils.randomAddOnKey();
    private static final String DASHBOARD_ITEM_DESCRIPTION = "Dashboard item description";

    private static final String DASHBOARD_ITEM_KEY = "dashboard-item-key";
    private static final String DASHBOARD_ITEM_TITLE = "Dashboard item title";

    private static final String NON_CONFIGURABLE_DASHBOARD_ITEM_KEY = "dashboard-item-key-non-conf";
    private static final String NON_CONFIGURABLE_DASHBOARD_ITEM_TITLE = "Dashboard item title non configurable";

    private static final String VENDOR_NAME = "Atlassian";
    private static final String DASHBOARD_ITEM_ID_QUERY_PARAM = "dashboardItemId";
    private static final String DASHBOARD_ID_QUERY_PARAM = "dashboardId";
    private static final TestUser TEST_USER = new TestUser("admin");
    private static ConnectRunner addon;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        logout();

        addon = new ConnectRunner(product, ADDON_KEY)
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName(VENDOR_NAME).withUrl("http://www.atlassian.com").build())
                .addModules("jiraDashboardItems",
                        buildDashboardItemModule(DASHBOARD_ITEM_TITLE, DASHBOARD_ITEM_KEY, true),
                        buildDashboardItemModule(NON_CONFIGURABLE_DASHBOARD_ITEM_TITLE, NON_CONFIGURABLE_DASHBOARD_ITEM_KEY, false))
                .addRoute("/dashboard-item-test", ConnectAppServlets.dashboardItemServlet(Lists.newArrayList(
                        new TestServletContextExtractor(DASHBOARD_ITEM_ID_QUERY_PARAM),
                        new TestServletContextExtractor(DASHBOARD_ID_QUERY_PARAM))))
                .addScopes(ScopeName.READ, ScopeName.WRITE, ScopeName.DELETE)
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
    public void testAddingDashboardItemToDashboard()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());
        addDashboardItemToDashboard(dashboard);

        Gadget gadgetByTitle = bindDashboardPage()
                .gadgets()
                .getByTitle(DASHBOARD_ITEM_TITLE);

        assertNotNull("Dashboard contains item with title", gadgetByTitle);
    }

    @Test
    public void testTwoDashboardItemsWithTheSameKeyAddedSuccessfulyToTheDashboard()
    {
        final DashboardPage dashboard = createDashboard(generateDashboardTitle());

        dashboard.gadgets()
                .openAddGadgetDialog()
                .searchFor(DASHBOARD_ITEM_TITLE)
                .addGadget(DASHBOARD_ITEM_TITLE)
                .addGadget(DASHBOARD_ITEM_TITLE)
                .simpleClose();

        final Iterable<PageElement> allGadgets = bindConnectDashboardPage()
                .getAllGadgets();

        assertThat(allGadgets, Matchers.<PageElement>iterableWithSize(2));
    }

    @Test
    public void testDashboardItemVisibleInDirectory()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        AddGadgetDialog gadgetDialog = dashboard.gadgets()
                .openAddGadgetDialog()
                .loadAllVisibleGadgets();

        assertThat("Dashboard item visible in directory", gadgetDialog.isGadgetVisible(DASHBOARD_ITEM_TITLE), is(true));
    }

    @Test
    public void testDashboardItemHasProperDescription()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        AddGadgetDialog gadgetDialog = dashboard.gadgets()
                .openAddGadgetDialog()
                .loadAllVisibleGadgets();

        assertThat(gadgetDialog.getGadgetDescription(DASHBOARD_ITEM_TITLE), is(DASHBOARD_ITEM_DESCRIPTION));
    }

    @Test
    public void testDashboardItemHasProperVendorInformation()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        AddGadgetDialog gadgetDialog = dashboard.gadgets()
                .openAddGadgetDialog()
                .loadAllVisibleGadgets();

        assertThat(gadgetDialog.getGadgetAuthor(DASHBOARD_ITEM_TITLE), is(VENDOR_NAME));
    }

    @Test
    public void testDashboardItemContentIsDisplayed()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());
        addDashboardItemToDashboard(dashboard);

        ConnectDashboardItemElement dashboardItem = getOnlyDashboardItem();

        assertNotNull("Dashboard item id query parameter was not passed", dashboardItem.getDashboardItemId());
        assertNotNull("Dashboard id query parameter was not passed", dashboardItem.getDashboardId());
    }

    @Test
    public void testDashboardItemCanReadProperties() throws JSONException
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());
        addDashboardItemToDashboard(dashboard);

        ConnectDashboardItemElement dashboardItem = getOnlyDashboardItem();

        assertThat(dashboardItem.getPropertiesStatus(), is("200"));

        JSONObject properties = new JSONObject(dashboardItem.getProperties());

        assertThat(properties.getJSONArray("keys").length(), is(0));
    }

    @Test
    public void testDashboardItemReactsToEditClick()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        closeBaseUrlBanner();

        addDashboardItemToDashboard(dashboard);

        GadgetMenu dashboardItemMenu = bindDashboardPage()
                .gadgets()
                .getByTitle(DASHBOARD_ITEM_TITLE)
                .openMenu();

        assertTrue("Dashboard item is not configurable", dashboardItemMenu.hasEditLink());

        ConnectDashboardItemElement dashboardItem = getOnlyDashboardItem();

        dashboardItemMenu.selectByClassName("configure");

        dashboardItem.assertEditClicked();
    }

    @Test
    public void testDashboardItemCanSetTitle()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());
        addDashboardItemToDashboard(dashboard);

        ConnectDashboardPageWrapper connectDashboard = bindConnectDashboardPage();
        ConnectDashboardItemElement dashboardItem = getOnlyElement(connectDashboard.getDashboardItems(ADDON_KEY, DASHBOARD_ITEM_KEY));

        dashboardItem.changeTitle();

        final Gadget gadgetByTitle = bindDashboardPage()
                .gadgets()
                .getByTitle("Setting title works");

        assertNotNull(gadgetByTitle);
    }

    @Test
    public void testDashboardItemCanSeeThatItIsEditable()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());
        addDashboardItemToDashboard(dashboard, DASHBOARD_ITEM_TITLE);

        ConnectDashboardPageWrapper connectDashboard = bindConnectDashboardPage();
        ConnectDashboardItemElement dashboardItem = getOnlyElement(connectDashboard.getDashboardItems(ADDON_KEY, DASHBOARD_ITEM_KEY));

        assertTrue(dashboardItem.getEditable());
    }

    @Test
    public void testDashboardItemCanSeeThatItIsNotEditable()
    {
        DashboardPage dashboard = createDashboard(generateDashboardTitle());
        addDashboardItemToDashboard(dashboard, NON_CONFIGURABLE_DASHBOARD_ITEM_TITLE);

        ConnectDashboardPageWrapper connectDashboard = bindConnectDashboardPage();
        ConnectDashboardItemElement dashboardItem = getOnlyElement(connectDashboard.getDashboardItems(ADDON_KEY, NON_CONFIGURABLE_DASHBOARD_ITEM_KEY));

        assertFalse(dashboardItem.getEditable());
    }

    @Test
    public void testDashboardItemRespectConditionForDirectoryView() throws Exception
    {
        String title = "Dashboard item with condition";
        String moduleKey = "dashboard-item-with-condition";
        DashboardItemConditionServlet conditionServlet =
                new DashboardItemConditionServlet(TEST_USER.getUsername(), Lists.newArrayList("directory", "default"), moduleKey);
        ConnectRunner addOnRunner = new ConnectRunner(product, AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName(VENDOR_NAME).withUrl("http://www.atlassian.com").build())
                .addModules("jiraDashboardItems",
                        DashboardItemModuleBean.newBuilder()
                                .withDescription(new I18nProperty("Description", "description.i18n"))
                                .withName(new I18nProperty(title, "title.i18n"))
                                .withThumbnailUrl("atlassian-icon-16.png")
                                .withUrl("/item-with-condition?dashboardItemId={dashboardItem.id}&dashboardId={dashboard.id}&view={dashboardItem.viewType}")
                                .withConditions(DashboardItemConditionServlet.conditionBean())
                                .withKey(moduleKey)
                                .configurable(true)
                                .build())
                .addRoute("/item-with-condition", ConnectAppServlets.dashboardItemServlet(Lists.newArrayList(
                        new TestServletContextExtractor(DASHBOARD_ITEM_ID_QUERY_PARAM),
                        new TestServletContextExtractor(DASHBOARD_ID_QUERY_PARAM))))
                .addRoute(DashboardItemConditionServlet.DASHBOARD_ITEM_CONDITION_URL,
                        conditionServlet)
                .addScopes(ScopeName.READ, ScopeName.WRITE, ScopeName.DELETE)
                .start();

        DashboardPage dashboard = createDashboard(generateDashboardTitle());

        addDashboardItemToDashboard(dashboard, title);
        addDashboardItemToDashboard(dashboard, DASHBOARD_ITEM_TITLE);

        GadgetContainer gadgetContainer = bindDashboardPage().gadgets();
        Gadget gadgetWithCondition = gadgetContainer.getByTitle(title);
        Gadget gadgetWithoutCondition = gadgetContainer.getByTitle(DASHBOARD_ITEM_TITLE);

        assertNotNull("Dashboard item with condition should be visible", gadgetWithCondition);
        assertNotNull("Dashboard item without condition should be visible", gadgetWithoutCondition);

        // change supported view of dashboard item to directory only
        conditionServlet.setSupportedViewMode("directory");

        product.getTester().getDriver().navigate().refresh();

        Iterable<ConnectDashboardItemElement> dashboardItemsAfterRefresh =
                bindConnectDashboardPage().getDashboardItems(addOnRunner.getAddon().getKey(), moduleKey);

        // dashboard item is not visible in default view
        assertThat(dashboardItemsAfterRefresh, Matchers.emptyIterable());

        addOnRunner.stopAndUninstall();
    }

    private static DashboardItemModuleBean buildDashboardItemModule(String title, String key, boolean configurable)
    {
        return DashboardItemModuleBean.newBuilder()
                .withDescription(new I18nProperty(DASHBOARD_ITEM_DESCRIPTION, "description.i18n.key"))
                .withName(new I18nProperty(title, null))
                .withThumbnailUrl("atlassian-icon-16.png")
                .withUrl("/dashboard-item-test?dashboardItemId={dashboardItem.id}&dashboardId={dashboard.id}&view={dashboardItem.viewType}")
                .withKey(key)
                .configurable(configurable)
                .build();
    }

    private DashboardPage bindDashboardPage()
    {
        return product.getPageBinder().bind(DashboardPage.class);
    }

    private ConnectDashboardItemElement getOnlyDashboardItem()
    {
        ConnectDashboardPageWrapper connectDashboard = bindConnectDashboardPage();
        Iterable<ConnectDashboardItemElement> dashboardItems = connectDashboard.getDashboardItems(ADDON_KEY, DASHBOARD_ITEM_KEY);

        assertThat(dashboardItems, Matchers.<ConnectDashboardItemElement>iterableWithSize(1));
        return getOnlyElement(dashboardItems);
    }

    private void addDashboardItemToDashboard(final DashboardPage dashboard)
    {
        addDashboardItemToDashboard(dashboard, DASHBOARD_ITEM_TITLE);
    }

    private void addDashboardItemToDashboard(final DashboardPage dashboard, final String title)
    {
        dashboard.gadgets()
                .openAddGadgetDialog()
                .searchFor(title)
                .addGadget(title)
                .simpleClose();
    }

    private ConnectDashboardPageWrapper bindConnectDashboardPage() {return product.getPageBinder().bind(ConnectDashboardPageWrapper.class);}

    private String generateDashboardTitle() {return RandomStringUtils.random(20);}

    private DashboardPage createDashboard(final String dashboardName)
    {
        final AddDashboardPage addDashboardPage = loginAndVisit(TEST_USER, AddDashboardPage.class);
        addDashboardPage.setName(dashboardName);

        addDashboardPage.submit();

        return product.visit(DashboardPage.class);
    }

    public static class ConnectDashboardPageWrapper extends GadgetContainer
    {
        @Inject
        private PageElementFinder elementFinder;

        public Iterable<PageElement> getAllGadgets()
        {
            return Iterables.filter(dashboard.findAll(By.className("dashboard-item-title")), new Predicate<PageElement>()
            {
                @Override
                public boolean apply(final PageElement pageElement)
                {
                    return !pageElement.getAttribute("id").isEmpty();
                }
            });
        }

        public Iterable<ConnectDashboardItemElement> getDashboardItems(final String addOnKey, final String moduleKey)
        {
            final List<PageElement> iFrameContainers = elementFinder.findAll(By.className("iframe-init"));
            final Iterable<PageElement> gadgetsContainers = Iterables.filter(iFrameContainers, new Predicate<PageElement>()
            {
                @Override
                public boolean apply(final PageElement pageElement)
                {
                    return pageElement.getAttribute("id").contains(ModuleKeyUtils.addonAndModuleKey(addOnKey, moduleKey));
                }
            });
            return Iterables.transform(gadgetsContainers, new Function<PageElement, ConnectDashboardItemElement>()
            {
                @Override
                public ConnectDashboardItemElement apply(final PageElement pageElement)
                {
                    final String id = pageElement.getAttribute("id");
                    final String pageKey = id.substring(id.indexOf(moduleKey));
                    return pageBinder.bind(ConnectDashboardItemElement.class, addOnKey, pageKey);
                }
            });
        }
    }

    public static class ConnectDashboardItemElement extends ConnectAddOnEmbeddedTestPage
    {
        @Inject
        protected PageElementFinder elementFinder;

        public ConnectDashboardItemElement(final String addOnKey, final String pageElementKey)
        {
            super(addOnKey, pageElementKey, true);
        }

        public String getDashboardItemId()
        {
            return getValueById("dashboardItemId");
        }

        public String getDashboardId()
        {
            return getValueById("dashboardId");
        }

        public String getPropertiesStatus()
        {
            return waitForValue("propertiesStatus");
        }

        public String getProperties()
        {
            return waitForValue("properties");
        }

        public boolean getEditable()
        {
            return Boolean.valueOf(waitForValue("editable"));
        }

        public void assertEditClicked()
        {
            runInFrame(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    Poller.waitUntilTrue(elementFinder.find(By.id("editBox")).timed().isVisible());
                    return null;
                }
            });
        }

        public void changeTitle()
        {
            runInFrame(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    elementFinder.find(By.id("set-title")).click();
                    return null;
                }
            });
        }
    }

    private void closeBaseUrlBanner()
    {
        try
        {
            WebElement banner = product.getTester().getDriver().findElement(By.xpath("//div[@id='aui-flag-container']"));
            WebElement closeButton = banner.findElement(By.xpath("//span[@role='button']"));
            closeButton.click();
        }
        catch (RuntimeException ex)
        {
            // apparently the banner is not present
        }
    }
}
