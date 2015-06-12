package it.jira.jsapi;

import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.RemoteQuickCreateIssueGeneralPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.SUMMARY;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;
import static it.modules.ConnectAsserts.verifyContainsStandardAddOnQueryParamters;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * Test of general page in JIRA
 */
public class TestJiraIssueCreate extends JiraWebDriverTestBase
{
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String PAGE_NAME = "My Awesome Page";

    private static ConnectRunner remotePlugin;

    private static String addonKey;
    private String awesomePageModuleKey;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        addonKey = AddonTestUtils.randomAddOnKey();
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?project_id={project.id}&project_key={project.key}")
                                .withConditions(toggleableConditionBean())
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.quickCreateIssueServlet())
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
    public void beforeEachTest()
    {
        this.awesomePageModuleKey = addonAndModuleKey(addonKey,KEY_MY_AWESOME_PAGE);
    }

    @Test
    public void canReceiveCreatedIssuesFromAddon() throws MalformedURLException, URISyntaxException
    {
        loginAndVisit(testUserFactory.basicUser(), JiraViewProjectPage.class, project.getKey());
        RemoteQuickCreateIssueGeneralPage generalPage = loginAndVisit(testUserFactory.basicUser(), RemoteQuickCreateIssueGeneralPage.class, addonKey, KEY_MY_AWESOME_PAGE);

        generalPage.launchQuickCreate();

        CreateIssueDialog createIssueDialog = product.getPageBinder().bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());
        createIssueDialog.switchToFullMode();

        createIssueDialog.fill("summary", "test");
        createIssueDialog.submit(GlobalMessage.class);
        assertEquals(generalPage.getCreatedIssueSummary(), "test");

    }
}