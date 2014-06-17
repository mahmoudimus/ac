package it.capabilities.jira;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.projects.pageobjects.page.BrowseProjectPage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.IssueNavigatorViewsMenu;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdminPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdministrationHomePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdvancedSearchPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraComponentTabPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraVersionTabPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.Section;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import it.servlet.ConnectAppServlets;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
import java.net.MalformedURLException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean.newSearchRequestViewModuleBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.jira.TestJira.EXTRA_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEscaping extends TestBase
{
    private static final String MODULE_NAME = "<b>${user}</b>";
    private static final String MODULE_NAME_JIRA_ESCAPED = "<b>\\${user}</b>";

    private static final String GENERAL_PAGE_KEY = "general-page";
    private static final String WEB_ITEM_KEY_1 = "web-item-1";
    private static final String WEB_ITEM_KEY_2 = "web-item-2";
    private static final String ADMIN_PAGE_KEY = "admin-page";
    private static final String COMPONENT_TAB_PANEL_KEY = "component-tab-panel";
    private static final String ISSUE_TAB_PANEL_KEY = "issue-tab-panel";
    private static final String PROFILE_TAB_PANEL_KEY = "profile-tab-panel";
    private static final String PROJECT_ADMIN_TAB_PANEL_KEY = "project-admin-tab-panel";
    private static final String PROJECT_TAB_PANEL_KEY = "project-tab-panel";
    private static final String VERSION_TAB_PANEL_KEY = "version-tab-panel";
    private static final String SEARCH_REQUEST_VIEW_KEY = "search-request-view";
    private static final String WEB_PANEL_KEY = "web-panel";
    private static final String WORKFLOW_POST_FUNCTION_KEY = "workflow-post-function";

    private static final String MODULE_URL = "/page";

    private static final String PROJECT_KEY = RandomStringUtils.randomAlphabetic(4).toUpperCase();
    private static final String WORKFLOW_NAME = "classic default workflow";
    private static final Integer WORKFLOW_STEP = 1;
    private static final Integer WORKFLOW_TRANSITION = 5;

    private static ConnectRunner runner;
    private static ConnectPageOperations connectPageOperations = new ConnectPageOperations(jira().getPageBinder(),
            jira().getTester().getDriver());

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(jira().getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
                .addAuth(AuthenticationBean.none())
                .addModule("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(GENERAL_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WEB_ITEM_KEY_1)
                                .withUrl(MODULE_URL)
                                .withContext(AddOnUrlContext.addon)
                                .withLocation("system.top.navigation.bar")
                                .withTooltip(new I18nProperty(MODULE_NAME, null))
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WEB_ITEM_KEY_2)
                                .withUrl(MODULE_URL)
                                .withContext(AddOnUrlContext.addon)
                                .withLocation("system.user.options/jira-help")
                                .withTooltip(new I18nProperty(MODULE_NAME, null))
                                .build()
                )
                .addModule("adminPages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(ADMIN_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("jiraComponentTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(COMPONENT_TAB_PANEL_KEY)
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
                .addModule("jiraVersionTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(VERSION_TAB_PANEL_KEY)
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

        backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testGeneralPage() throws MalformedURLException
    {
        jira().quickLoginAsAdmin();
        RemoteWebItem webItem = findWebItem(GENERAL_PAGE_KEY);
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItemInGlobalNav() throws MalformedURLException
    {
        jira().quickLoginAsAdmin();
        RemoteWebItem webItem = findWebItem(WEB_ITEM_KEY_1);
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItemInHelpMenu() throws MalformedURLException
    {
        jira().quickLoginAsAdmin();
        RemoteWebItem webItem = findWebItem(WEB_ITEM_KEY_2);
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testAdminPage() throws MalformedURLException
    {
        jira().quickLoginAsAdmin(JiraAdministrationHomePage.class, EXTRA_PREFIX);
        JiraAdminPage adminPage = jira().getPageBinder().bind(JiraAdminPage.class, getModuleKey(ADMIN_PAGE_KEY));
        assertIsEscaped(adminPage.getRemotePluginLinkText());
    }

    @Test
    public void testComponentTabPanel() throws MalformedURLException
    {
        ComponentClient componentClient = new ComponentClient(jira().environmentData());
        Component component = componentClient.create(new Component()
                .name("Component Tab Panel Test")
                .project(PROJECT_KEY));

        JiraComponentTabPage componentTabPage = jira().quickLoginAsAdmin(JiraComponentTabPage.class, PROJECT_KEY,
                component.id.toString(), ConnectPluginInfo.getPluginKey(), getModuleKey(COMPONENT_TAB_PANEL_KEY));

        assertIsEscaped(componentTabPage.findAddOnTab().getText());
    }

    @Test
    public void testIssueTabPanel() throws MalformedURLException
    {
        IssueCreateResponse issue = jira().backdoor().issues().createIssue(PROJECT_KEY, "test issue tab panel");
        JiraViewIssuePageWithRemotePluginIssueTab page = jira().quickLoginAsAdmin(JiraViewIssuePageWithRemotePluginIssueTab.class,
                getModuleKey(ISSUE_TAB_PANEL_KEY), issue.key(), runner.getAddon().getKey(), ConnectPluginInfo.getPluginKey() + ":");
        assertIsEscaped(page.getTabName());
    }

    @Test
    public void testProfileTabPanel() throws MalformedURLException
    {
        jira().quickLoginAsAdmin(ViewProfilePage.class);
        String moduleKey = getModuleKey(PROFILE_TAB_PANEL_KEY);
        LinkedRemoteContent tabPanel = connectPageOperations.findTabPanel("up_" + moduleKey + "_a",
                Option.<String>none(), moduleKey);
        assertIsEscaped(tabPanel.getWebItem().getLinkText());
    }

    @Test
    public void testProjectAdminTabPanel() throws MalformedURLException
    {
        final String moduleKey = getModuleKey(PROJECT_ADMIN_TAB_PANEL_KEY);
        ProjectSummaryPageTab page = jira().quickLoginAsAdmin(ProjectSummaryPageTab.class, PROJECT_KEY);
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
    public void testProjectTabPanel() throws MalformedURLException
    {
        jira().quickLoginAsAdmin(BrowseProjectPage.class, PROJECT_KEY);
        String moduleKey = ConnectPluginInfo.getPluginKey() + ":" + getModuleKey(PROJECT_TAB_PANEL_KEY) + "-panel";
        LinkedRemoteContent tabPanel = connectPageOperations.findTabPanel(moduleKey, Option.<String>none(), moduleKey);
        assertIsEscaped(tabPanel.getWebItem().getLinkText());
    }

    @Test
    public void testVersionTabPanel() throws MalformedURLException
    {
        VersionClient versionClient = new VersionClient(jira().environmentData());
        Version version = versionClient.create(new Version()
                .name("Test Version Tab Panel")
                .project(PROJECT_KEY));

        JiraVersionTabPage versionTabPage = jira().quickLoginAsAdmin(JiraVersionTabPage.class, PROJECT_KEY,
                version.id.toString(), ConnectPluginInfo.getPluginKey(), getModuleKey(VERSION_TAB_PANEL_KEY));

        assertIsEscaped(versionTabPage.findAddOnTab().getText());
    }

    @Test
    public void testSearchRequestView() throws MalformedURLException
    {
        JiraAdvancedSearchPage searchPage = jira().quickLoginAsAdmin(JiraAdvancedSearchPage.class);
        searchPage.enterQuery("project = " + PROJECT_KEY).submit();
        IssueNavigatorViewsMenu viewsMenu = searchPage.viewsMenu().open();
        IssueNavigatorViewsMenu.ViewEntry entry = viewsMenu.entryWithLabel(MODULE_NAME_JIRA_ESCAPED);
        assertTrue(entry.isPresent());
    }

    @Test
    public void testWebPanel() throws MalformedURLException
    {
        IssueCreateResponse issue = jira().backdoor().issues().createIssue(PROJECT_KEY, "test web panel");
        JiraViewIssuePage page = jira().quickLoginAsAdmin(JiraViewIssuePage.class, issue.key());
        Section section = page.getSection(getModuleKey(WEB_PANEL_KEY));
        assertIsEscaped(section.getTitle());
    }

    @Test
    public void testWorkflowPostFunction() throws MalformedURLException
    {

    }

    private void assertIsEscaped(String text)
    {
        // JIRA's own escaping leaves a '\' in front of the '$', which seems wrong, so checking both flavours
        if (!MODULE_NAME.equals(text) && !MODULE_NAME_JIRA_ESCAPED.equals(text))
        {
            assertEquals(MODULE_NAME, text);
        }
    }

    private RemoteWebItem findWebItem(String moduleKey)
    {
        JiraViewProjectPage viewProjectPage = jira().visit(JiraViewProjectPage.class, PROJECT_KEY);
        return viewProjectPage.findWebItem(getModuleKey(moduleKey), Optional.<String>absent());
    }

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(runner.getAddon().getKey(), module);
    }
}
