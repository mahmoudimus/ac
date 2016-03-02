package it.jira.condition;

import com.atlassian.connect.test.jira.pageobjects.ViewIssuePageWithAddonFragments;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectTabPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestEntityPropertyEqualToCondition extends JiraWebDriverTestBase {
    private static final String WEB_PANEL_KEY = "issue-property-web-panel";
    private static final String TAB_PANEL_KEY = "issue-property-tab-panel";
    private static final String WEB_PANEL_PROP = "prop";
    private static final String TAB_PANEL_PROP = "tab-panel-prop";

    private static ConnectRunner remotePlugin;
    private EntityPropertyClient issueEntityPropertyClient;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        remotePlugin = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addJWT(ConnectAppServlets.installHandlerServlet())
                .addModules("webPanels",
                        new WebPanelModuleBeanBuilder()
                                .withKey(WEB_PANEL_KEY)
                                .withLocation("atl.jira.view.issue.right.context")
                                .withName(new I18nProperty("issue-property-web-panel", null))
                                .withConditions(new SingleConditionBeanBuilder()
                                        .withCondition("entity_property_equal_to")
                                        .withParam("propertyKey", WEB_PANEL_PROP)
                                        .withParam("entity", "issue")
                                        .withParam("value", "true")
                                        .build())
                                .withUrl("/content")
                                .build())
                .addModules("jiraIssueTabPanels",
                    new ConnectTabPanelModuleBeanBuilder()
                        .withKey(TAB_PANEL_KEY)
                        .withName(new I18nProperty("issue-property-tab-panel", null))
                        .withUrl("/tab-panel-content")
                        .withConditions(new SingleConditionBeanBuilder()
                            .withCondition("entity_property_equal_to")
                            .withParam("propertyKey", TAB_PANEL_PROP)
                            .withParam("value", "true")
                            .withParam("entity", "issue")
                            .build()
                        )
                        .build()
                )
                .addRoute("/content", ConnectAppServlets.customMessageServlet("Web panel displayed"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void setUp() {
        issueEntityPropertyClient = new EntityPropertyClient(product.environmentData(), "issue");
        login(testUserFactory.admin());
    }

    @After
    public void tearDown() {
        login(testUserFactory.admin());
    }

    @Test
    public void webPanelShouldBeVisibleIfIssuePropertyIsSetToTrue() throws RemoteException, JSONException {

        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), WEB_PANEL_PROP, json("true"));

        assertThat(webPanelIsVisible(WEB_PANEL_KEY, issue), equalTo(true));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfIssuePropertyIsSetToFalse() throws JSONException, RemoteException {
        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), WEB_PANEL_PROP, json("false"));

        assertThat(webPanelIsVisible(WEB_PANEL_KEY, issue), equalTo(false));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfIssuePropertyIsNotSet() throws JSONException, RemoteException {
        IssueCreateResponse issue = createIssue();
        assertThat(webPanelIsVisible(WEB_PANEL_KEY, issue), equalTo(false));
    }

    @Test
    public void issueTabPanelShouldBeVisibleIfIssuePropertyIsSetToTrue() throws RemoteException {
        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), TAB_PANEL_PROP, json("true"));

        assertThat(issueTabPanelIsVisible(TAB_PANEL_KEY, issue), equalTo(true));
    }

    @Test
    public void issueTabPanelShouldNotBeVisibleIfIssuePropertyIsSetToFalse() throws RemoteException {
        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), TAB_PANEL_PROP, json("false"));

        assertThat(issueTabPanelIsVisible(TAB_PANEL_KEY, issue), equalTo(false));
    }

    @Test
    public void issueTabPanelShouldNotBeVisibleIfIssuePropertyIsNotSet() throws RemoteException {
        IssueCreateResponse issue = createIssue();
        assertThat(issueTabPanelIsVisible(TAB_PANEL_KEY, issue), equalTo(false));
    }

    private IssueCreateResponse createIssue() throws RemoteException {
        return product.backdoor().issues().createIssue(project.getKey(), "Test issue");
    }

    private boolean webPanelIsVisible(String panelKey, final IssueCreateResponse issue) {
        product.visit(ViewIssuePageWithAddonFragments.class, issue.key());
        return connectPageOperations.existsWebPanel(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), panelKey));
    }

    private boolean issueTabPanelIsVisible(String tabPanelKey, final IssueCreateResponse issue) {
        product.visit(ViewIssuePageWithAddonFragments.class, issue.key());
        return connectPageOperations.existsWebItem(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), tabPanelKey));
    }

    private JSONObject json(final String representation) {
        return new JSONObject() {
            @Override
            public String toString() {
                return representation;
            }
        };
    }
}
