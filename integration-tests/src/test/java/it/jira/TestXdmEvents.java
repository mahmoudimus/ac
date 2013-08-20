package it.jira;

import com.atlassian.plugin.connect.test.pageobjects.RemoteXdmEventPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.RemoteWebPanelModule;
import hudson.plugins.jira.soap.RemoteIssue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;
import static org.junit.Assert.*;

public class TestXdmEvents extends JiraWebDriverTestBase
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
            .add(RemoteWebPanelModule.key("xdm-events-1")
                .name("XDM Events Panel 1")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanel1")
                .resource(newServlet(new XdmEventsPanelServlet(1))))
            .add(RemoteWebPanelModule.key("xdm-events-2")
                .name("XDM Events Panel 2")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanel2")
                .resource(newServlet(new XdmEventsPanelServlet(2))))
            .add(RemoteWebPanelModule.key("xdm-events-3")
                .name("XDM Events Panel 3")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanel3")
                .resource(newServlet(new XdmEventsPanelServlet(3))))
            .start();
    }

    @Test
    public void testXdmEvents() throws Exception
    {
        loginAsAdmin();

        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());

        RemoteXdmEventPanel panel1 = viewIssuePage.findXdmEventPanel("xdm-events-1");
        RemoteXdmEventPanel panel2 = viewIssuePage.findXdmEventPanel("xdm-events-2");
        RemoteXdmEventPanel panel3 = viewIssuePage.findXdmEventPanel("xdm-events-3");

        assertEquals(panel1.getPanelId(), "1");
        assertEquals(panel2.getPanelId(), "2");
        assertEquals(panel3.getPanelId(), "3");

        panel1.emit();
        assertTrue(panel1.hasLoggedEvent("panel-1", "event-1"));
        assertTrue(panel2.hasLoggedEvent("panel-1", "event-1"));
        assertTrue(panel3.hasLoggedEvent("panel-1", "event-1"));

        panel2.emit();
        assertTrue(panel1.hasLoggedEvent("panel-2", "event-1"));
        assertTrue(panel2.hasLoggedEvent("panel-2", "event-1"));
        assertTrue(panel3.hasLoggedEvent("panel-2", "event-1"));

        panel3.emit();
        assertTrue(panel1.hasLoggedEvent("panel-3", "event-1"));
        assertTrue(panel2.hasLoggedEvent("panel-3", "event-1"));
        assertTrue(panel3.hasLoggedEvent("panel-3", "event-1"));

        panel1.emit();
        assertTrue(panel1.hasLoggedEvent("panel-1", "event-2"));
        assertTrue(panel2.hasLoggedEvent("panel-1", "event-2"));
        assertTrue(panel3.hasLoggedEvent("panel-1", "event-2"));

        panel2.emit();
        assertTrue(panel1.hasLoggedEvent("panel-2", "event-2"));
        assertTrue(panel2.hasLoggedEvent("panel-2", "event-2"));
        assertTrue(panel3.hasLoggedEvent("panel-2", "event-2"));

        panel3.emit();
        assertTrue(panel1.hasLoggedEvent("panel-3", "event-2"));
        assertTrue(panel2.hasLoggedEvent("panel-3", "event-2"));
        assertTrue(panel3.hasLoggedEvent("panel-3", "event-2"));
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }
}
