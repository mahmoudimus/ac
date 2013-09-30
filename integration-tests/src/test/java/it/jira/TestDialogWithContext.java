package it.jira;

import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
import com.atlassian.plugin.connect.test.server.module.RemoteWebPanelModule;
import hudson.plugins.jira.soap.RemoteIssue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static org.junit.Assert.assertEquals;

public class TestDialogWithContext extends JiraWebDriverTestBase
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(RemoteWebPanelModule.key("my-issue-panel")
                        .name("Issue WebPanel")
                        .location("atl.jira.view.issue.left.context")
                        .path("/ilwp")
                        .resource(newMustacheServlet("iframe-open-dialog.mu")))
                .add(DialogPageModule.key("my-dialog")
                        .name("Remote dialog")
                        .path("/my-dialog?my-issue-id=${issue.id}")
                        .section("")
                        .resource(newMustacheServlet("iframe-close-dialog.mu")))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testOpenCloseDialogUrl() throws Exception
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());

        viewIssuePage.findWebPanel("my-issue-panel");

        RemoteDialogOpeningPage dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPage.class, "remote-web-panel", "my-issue-panel", remotePlugin.getPluginKey());
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey("servlet-my-dialog");

        assertEquals("test dialog close url", issue.getId(), closeDialogPage.getFromQueryString("my-issue-id"));
    }
}
