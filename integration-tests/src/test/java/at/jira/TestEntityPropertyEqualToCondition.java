package at.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import hudson.plugins.jira.soap.RemoteIssue;
import com.atlassian.plugin.connect.test.client.AddOnPropertyClient;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestEntityPropertyEqualToCondition extends JiraWebDriverTestBase
{
    private static ConnectRunner remotePlugin;
    private EntityPropertyClient issueEntityPropertyClient;
    private AddOnPropertyClient addOnPropertyClient;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addJWT(ConnectAppServlets.installHandlerServlet())
                .addModules("webPanels",
                        new WebPanelModuleBeanBuilder()
                                .withKey("issue-property-web-panel")
                                .withLocation("atl.jira.view.issue.right.context")
                                .withName(new I18nProperty("issue-property-web-panel", "issue-property-web-panel"))
                                .withConditions(new SingleConditionBeanBuilder()
                                        .withCondition("entity_property_equal_to")
                                        .withParam("propertyKey", "prop")
                                        .withParam("entity", "issue")
                                        .withParam("value", "true")
                                        .build())
                                .withUrl("/content")
                                .build(),
                        new WebPanelModuleBeanBuilder()
                                .withKey("add-on-property-web-panel")
                                .withLocation("atl.jira.view.issue.right.context")
                                .withName(new I18nProperty("add-on-property-web-panel", "add-on-property-web-panel"))
                                .withConditions(new SingleConditionBeanBuilder()
                                        .withCondition("entity_property_equal_to")
                                        .withParam("propertyKey", "prop")
                                        .withParam("entity", "addon")
                                        .withParam("value", "true")
                                        .build())
                                .withUrl("/content")
                                .build())
                .addRoute("/content", ConnectAppServlets.customMessageServlet("Web panel displayed"))
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

    @Before
    public void setUp()
    {
        issueEntityPropertyClient = new EntityPropertyClient(product.environmentData(), "issue");
        addOnPropertyClient = new AddOnPropertyClient(product, remotePlugin);
    }

    @Test
    public void webPanelShouldBeVisibleIfIssuePropertyIsSetToTrue() throws RemoteException, JSONException
    {

        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), "prop", json("true"));

        assertThat(webPanelIsVisible("issue-property-web-panel", issue), equalTo(true));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfIssuePropertyIsSetToFalse() throws JSONException, RemoteException
    {
        IssueCreateResponse issue = createIssue();

        issueEntityPropertyClient.put(issue.key(), "prop", json("false"));

        assertThat(webPanelIsVisible("issue-property-web-panel", issue), equalTo(false));
    }

    @Test
    public void webPanelShouldBeVisibleIfAddOnPropertyIsSetToTrue() throws Exception
    {
        IssueCreateResponse issue = createIssue();

        addOnPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "true");

        assertThat(webPanelIsVisible("add-on-property-web-panel", issue), equalTo(true));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfAddOnPropertyIsSetToFalse() throws Exception
    {
        IssueCreateResponse issue = createIssue();

        addOnPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPanelIsVisible("add-on-property-web-panel", issue), equalTo(false));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfAddOnPropertyIsNotSet() throws Exception
    {
        IssueCreateResponse issue = createIssue();

        addOnPropertyClient.deleteProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPanelIsVisible("add-on-property-web-panel", issue), equalTo(false));
    }

    private IssueCreateResponse createIssue() throws RemoteException
    {
        TestUser user = testUserFactory.basicUser();
        login(user);

        return product.backdoor().issues().createIssue(project.getKey(), "Test issue");
    }

    private boolean webPanelIsVisible(String panelKey, final IssueCreateResponse issue)
    {
        product.visit(JiraViewIssuePage.class, issue.key());
        return connectPageOperations.existsWebPanel(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), panelKey));
    }

    private JSONObject json(final String representation)
    {
        return new JSONObject()
        {
            @Override
            public String toString()
            {
                return representation;
            }
        };
    }
}
