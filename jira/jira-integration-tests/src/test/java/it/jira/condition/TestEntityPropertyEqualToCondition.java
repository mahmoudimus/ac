package it.jira.condition;

import com.atlassian.connect.test.jira.pageobjects.ViewIssuePageWithAddonFragments;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
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

    private static ConnectRunner remotePlugin;
    private EntityPropertyClient issueEntityPropertyClient;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        remotePlugin = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addJWT(ConnectAppServlets.installHandlerServlet())
                .addModules("webPanels",
                        new WebPanelModuleBeanBuilder()
                                .withKey("issue-property-web-panel")
                                .withLocation("atl.jira.view.issue.right.context")
                                .withName(new I18nProperty("issue-property-web-panel", null))
                                .withConditions(new SingleConditionBeanBuilder()
                                        .withCondition("entity_property_equal_to")
                                        .withParam("propertyKey", "prop")
                                        .withParam("entity", "issue")
                                        .withParam("value", "true")
                                        .build())
                                .withUrl("/content")
                                .build())
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

        issueEntityPropertyClient.put(issue.key(), "prop", json("true"));

        assertThat(webPanelIsVisible("issue-property-web-panel", issue), equalTo(true));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfIssuePropertyIsSetToFalse() throws JSONException, RemoteException {
        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), "prop", json("false"));

        assertThat(webPanelIsVisible("issue-property-web-panel", issue), equalTo(false));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfIssuePropertyIsNotSet() throws JSONException, RemoteException {
        IssueCreateResponse issue = createIssue();
        assertThat(webPanelIsVisible("issue-property-web-panel", issue), equalTo(false));
    }

    private IssueCreateResponse createIssue() throws RemoteException {
        return product.backdoor().issues().createIssue(project.getKey(), "Test issue");
    }

    private boolean webPanelIsVisible(String panelKey, final IssueCreateResponse issue) {
        product.visit(ViewIssuePageWithAddonFragments.class, issue.key());
        return connectPageOperations.existsWebPanel(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), panelKey));
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
