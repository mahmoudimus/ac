package it.jira.jsapi;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.connect.test.jira.pageobjects.JiraViewProjectPage;
import com.atlassian.connect.test.jira.pageobjects.RemoteQuickCreateIssueGeneralPage;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.jira.JiraWebDriverTestBase;
import it.jira.servlet.JiraAppServlets;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

/**
 * Test of general page in JIRA
 */
public class TestJiraIssueCreate extends JiraWebDriverTestBase
{
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String PAGE_NAME = "My Awesome Page";

    private static ConnectRunner remotePlugin;

    private static String addonKey;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        addonKey = AddonTestUtils.randomAddonKey();
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg?project_id={project.id}&project_key={project.key}")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", JiraAppServlets.quickCreateIssueServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
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
