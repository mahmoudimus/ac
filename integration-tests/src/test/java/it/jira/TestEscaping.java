package it.jira;

import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.projects.pageobjects.webdriver.page.sidebar.Sidebar;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.IssueNavigatorViewsMenu;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAddWorkflowTransitionPostFunctionPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdminPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdministrationHomePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdvancedSearchPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectSummaryPageWithAddonTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.Section;
import com.atlassian.plugin.connect.test.pageobjects.jira.WorkflowPostFunctionEntry;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.servlet.ConnectAppServlets;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean.newSearchRequestViewModuleBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestEscaping extends JiraWebDriverTestBase
{
    private static final String ADDON_KEY = AddonTestUtils.randomAddOnKey();

    private static final String MODULE_NAME = "<b>${user}</b>";
    private static final String MODULE_NAME_JIRA_ESCAPED = "<b>\\${user}</b>";

    private static final String GENERAL_PAGE_KEY = "general-page";
    private static final String WEB_ITEM_KEY = "web-item";
    private static final String ADMIN_PAGE_KEY = "admin-page";
    private static final String ISSUE_TAB_PANEL_KEY = "issue-tab-panel";
    private static final String PROFILE_TAB_PANEL_KEY = "profile-tab-panel";
    private static final String PROJECT_ADMIN_TAB_PANEL_KEY = "project-admin-tab-panel";
    private static final String PROJECT_TAB_PANEL_KEY = "project-tab-panel";
    private static final String SEARCH_REQUEST_VIEW_KEY = "search-request-view";
    private static final String WEB_PANEL_KEY = "web-panel";
    private static final String WORKFLOW_POST_FUNCTION_KEY = "workflow-post-function";

    private static final String MODULE_URL = "/page";

    private static final String PROJECT_KEY = RandomStringUtils.randomAlphabetic(4).toUpperCase();
    private static final String WORKFLOW_NAME = "classic default workflow";
    private static final String WORKFLOW_STEP = "3";
    private static final String WORKFLOW_TRANSITION = "5";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), ADDON_KEY)
                .setAuthenticationToNone()
                .addModule("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(GENERAL_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .withWeight(1) // avoid ending up in 'More' menu
                                .build()
                )
                .addModule("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WEB_ITEM_KEY)
                                .withUrl(MODULE_URL)
                                .withContext(AddOnUrlContext.addon)
                                .withLocation("system.top.navigation.bar")
                                .withTooltip(new I18nProperty(MODULE_NAME, null))
                                .withWeight(1) // avoid ending up in 'More' menu
                                .build()
                )
                .addModule("adminPages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(ADMIN_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("jiraIssueTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(ISSUE_TAB_PANEL_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("jiraProfileTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(PROFILE_TAB_PANEL_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("jiraProjectAdminTabPanels",
                        newProjectAdminTabPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(PROJECT_ADMIN_TAB_PANEL_KEY)
                                .withUrl(MODULE_URL)
                                .withLocation("projectgroup4")
                                .build()
                )
                .addModule("jiraProjectTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(PROJECT_TAB_PANEL_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("jiraSearchRequestViews",
                        newSearchRequestViewModuleBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(SEARCH_REQUEST_VIEW_KEY)
                                .withUrl(MODULE_URL)
                                .withDescription(new I18nProperty(MODULE_NAME, null))
                                .build()
                )
                .addModule("webPanels",
                        newWebPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WEB_PANEL_KEY)
                                .withUrl(MODULE_URL)
                                .withLocation("atl.jira.view.issue.right.context")
                                .build()
                )
                .addModule("jiraWorkflowPostFunctions",
                        newWorkflowPostFunctionBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WORKFLOW_POST_FUNCTION_KEY)
                                .withTriggered(new UrlBean(MODULE_URL))
                                .withDescription(new I18nProperty(MODULE_NAME, null))
                                .build()
                )
                .addRoute(MODULE_URL, ConnectAppServlets.helloWorldServlet())
                .start();

        product.backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
    }

    @After
    public void logOutCurrentUser()
    {
        logout();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
        product.backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testGeneralPage() throws Exception
    {
        RemoteWebItem webItem = findWebItem(GENERAL_PAGE_KEY);
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItem() throws Exception
    {
        RemoteWebItem webItem = findWebItem(WEB_ITEM_KEY);
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItemTooltip() throws Exception
    {
        RemoteWebItem webItem = findWebItem(WEB_ITEM_KEY);
        assertIsEscaped(webItem.getTitle());
    }

    @Test
    public void testAdminPage() throws Exception
    {
        product.quickLoginAsAdmin(JiraAdministrationHomePage.class);
        JiraAdminPage adminPage = product.getPageBinder().bind(JiraAdminPage.class, runner.getAddon().getKey(), ADMIN_PAGE_KEY);
        assertIsEscaped(adminPage.getRemotePluginLinkText());
    }

    @Test
    public void testIssueTabPanel() throws Exception
    {
        IssueCreateResponse issue = product.backdoor().issues().createIssue(PROJECT_KEY, "test issue tab panel");
        JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(JiraViewIssuePageWithRemotePluginIssueTab.class,
                ISSUE_TAB_PANEL_KEY, issue.key(), runner.getAddon().getKey());
        assertIsEscaped(page.getTabName());
    }

    @Test
    public void testProfileTabPanel() throws Exception
    {
        product.quickLoginAsAdmin(ViewProfilePage.class);
        String moduleKey = getModuleKey(PROFILE_TAB_PANEL_KEY);
        LinkedRemoteContent tabPanel = connectPageOperations.findTabPanel("up_" + moduleKey + "_a",
                Option.<String>none(), moduleKey);
        assertIsEscaped(tabPanel.getWebItem().getLinkText());
    }

    @Test
    public void testProjectAdminTabPanel() throws Exception
    {
        final String moduleKey = getModuleKey(PROJECT_ADMIN_TAB_PANEL_KEY);
        ProjectSummaryPageTab page = product.quickLoginAsAdmin(ProjectSummaryPageTab.class, PROJECT_KEY);
        ProjectConfigTabs.Tab tab = Iterables.find(page.getTabs().getTabs(), new Predicate<ProjectConfigTabs.Tab>()
        {
            @Override
            public boolean apply(@Nullable ProjectConfigTabs.Tab tab)
            {
                return moduleKey.equals(tab.getId());
            }
        });
        assertIsEscaped(tab.getName());
    }

    @Test
    public void testProjectTabPanel() throws Exception
    {
        JiraProjectSummaryPageWithAddonTab summaryPage
                = product.visit(JiraProjectSummaryPageWithAddonTab.class, PROJECT_KEY, ADDON_KEY, PROJECT_TAB_PANEL_KEY);
        summaryPage = summaryPage.expandAddonsList();
        Sidebar.SidebarLink addonLink = summaryPage.getSidebar().getLinkByName(MODULE_NAME_JIRA_ESCAPED);
        assertTrue(addonLink.isVisible().byDefaultTimeout());
    }

    @Test
    public void testSearchRequestView() throws Exception
    {
        JiraAdvancedSearchPage searchPage = product.visit(JiraAdvancedSearchPage.class);
        searchPage.enterQuery("project = " + PROJECT_KEY).submit();
        IssueNavigatorViewsMenu viewsMenu = searchPage.viewsMenu().open();
        IssueNavigatorViewsMenu.ViewEntry entry = viewsMenu.entryWithLabel(MODULE_NAME_JIRA_ESCAPED);
        assertTrue(entry.isPresent());
    }

    @Test
    public void testWebPanel() throws Exception
    {
        IssueCreateResponse issue = product.backdoor().issues().createIssue(PROJECT_KEY, "test web panel");
        JiraViewIssuePage page = product.visit(JiraViewIssuePage.class, issue.key());
        Section section = page.getSection(getModuleKey(WEB_PANEL_KEY));
        assertIsEscaped(section.getTitle());
    }

    @Test
    public void testWorkflowPostFunction() throws Exception
    {
        final String id = ConnectPluginInfo.getPluginKey() + ":" + getModuleKey(WORKFLOW_POST_FUNCTION_KEY);

        JiraAddWorkflowTransitionPostFunctionPage workflowTransitionPage = product.quickLoginAsAdmin(
                JiraAddWorkflowTransitionPostFunctionPage.class, "live", WORKFLOW_NAME, WORKFLOW_STEP, WORKFLOW_TRANSITION);
        WorkflowPostFunctionEntry entry = Iterables.find(workflowTransitionPage.getPostFunctions(), new Predicate<WorkflowPostFunctionEntry>()
        {
            @Override
            public boolean apply(@Nullable WorkflowPostFunctionEntry workflowPostFunctionEntry)
            {
                return id.equals(workflowPostFunctionEntry.getId());
            }
        });
        assertIsEscaped(entry.getName());
        assertIsEscaped(entry.getDescription());
    }

    private void assertIsEscaped(String text)
    {
        // Jira's own escaping leaves a '\' in front of the '$', which seems wrong, so checking both flavours
        // Note that we're checking against the original name, not an escaped version, as getText() returns the
        // unescaped text. If markup was interpreted, the tags would be missing in the text.
        assertThat(text, anyOf(is(MODULE_NAME), is(MODULE_NAME_JIRA_ESCAPED)));
    }

    private RemoteWebItem findWebItem(String moduleKey)
    {
        product.visit(JiraViewProjectPage.class, PROJECT_KEY);
        return connectPageOperations.findWebItem(getModuleKey(moduleKey), Optional.<String>absent());
    }

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(runner.getAddon().getKey(), module);
    }
}
