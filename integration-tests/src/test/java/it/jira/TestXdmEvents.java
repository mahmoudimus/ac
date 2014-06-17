package it.jira;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.RemoteXdmEventPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.RemoteWebPanelModule;
import hudson.plugins.jira.soap.RemoteIssue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@XmlDescriptor
public class TestXdmEvents extends JiraWebDriverTestBase
{
    private static AtlassianConnectAddOnRunner remotePluginA;
    private static AtlassianConnectAddOnRunner remotePluginB;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePluginA = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
            .add(RemoteWebPanelModule.key("xdm-events-a1")
                .name("XDM Events Panel A1")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanelA1")
                .resource(newServlet(new XdmEventsPanelServlet("A1"))))
            .add(RemoteWebPanelModule.key("xdm-events-a2")
                .name("XDM Events Panel A2")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanelA2")
                .resource(newServlet(new XdmEventsPanelServlet("A2"))))
            .add(RemoteWebPanelModule.key("xdm-events-a3")
                .name("XDM Events Panel A3")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanelA3")
                .resource(newServlet(new XdmEventsPanelServlet("A3"))))
            .start();

        remotePluginB = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
            .add(RemoteWebPanelModule.key("xdm-events-b1")
                .name("XDM Events Panel B1")
                .location("atl.jira.view.issue.right.context")
                .path("/xdmEventsPanelB1")
                .resource(newServlet(new XdmEventsPanelServlet("B1"))))
            .start();
    }

    @Test
    public void testXdmEvents() throws Exception
    {
        loginAsAdmin();

        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());

        RemoteXdmEventPanel panelA1 = viewIssuePage.findXdmEventPanel("xdm-events-a1");
        RemoteXdmEventPanel panelA2 = viewIssuePage.findXdmEventPanel("xdm-events-a2");
        RemoteXdmEventPanel panelA3 = viewIssuePage.findXdmEventPanel("xdm-events-a3");
        RemoteXdmEventPanel panelB1 = viewIssuePage.findXdmEventPanel("xdm-events-b1");

        assertEquals(panelA1.getPanelId(), "A1");
        assertEquals(panelA2.getPanelId(), "A2");
        assertEquals(panelA3.getPanelId(), "A3");
        assertEquals(panelB1.getPanelId(), "B1");

        panelA1.emit();
        assertTrue(panelA1.hasLoggedEvent("panel-A1", "event-1"));
        assertTrue(panelA2.hasLoggedEvent("panel-A1", "event-1"));
        assertTrue(panelA3.hasLoggedEvent("panel-A1", "event-1"));
        assertTrue(panelB1.hasNotLoggedEvent("panel-A1", "event-1"));

        panelA2.emit();
        assertTrue(panelA1.hasLoggedEvent("panel-A2", "event-1"));
        assertTrue(panelA2.hasLoggedEvent("panel-A2", "event-1"));
        assertTrue(panelA3.hasLoggedEvent("panel-A2", "event-1"));
        assertTrue(panelB1.hasNotLoggedEvent("panel-A2", "event-1"));

        panelA3.emit();
        assertTrue(panelA1.hasLoggedEvent("panel-A3", "event-1"));
        assertTrue(panelA2.hasLoggedEvent("panel-A3", "event-1"));
        assertTrue(panelA3.hasLoggedEvent("panel-A3", "event-1"));
        assertTrue(panelB1.hasNotLoggedEvent("panel-A3", "event-1"));

        panelA1.emit();
        assertTrue(panelA1.hasLoggedEvent("panel-A1", "event-2"));
        assertTrue(panelA2.hasLoggedEvent("panel-A1", "event-2"));
        assertTrue(panelA3.hasLoggedEvent("panel-A1", "event-2"));
        assertTrue(panelB1.hasNotLoggedEvent("panel-A1", "event-2"));

        panelA2.emit();
        assertTrue(panelA1.hasLoggedEvent("panel-A2", "event-2"));
        assertTrue(panelA2.hasLoggedEvent("panel-A2", "event-2"));
        assertTrue(panelA3.hasLoggedEvent("panel-A2", "event-2"));
        assertTrue(panelB1.hasNotLoggedEvent("panel-A2", "event-2"));

        panelA3.emit();
        assertTrue(panelA1.hasLoggedEvent("panel-A3", "event-2"));
        assertTrue(panelA2.hasLoggedEvent("panel-A3", "event-2"));
        assertTrue(panelA3.hasLoggedEvent("panel-A3", "event-2"));
        assertTrue(panelB1.hasNotLoggedEvent("panel-A3", "event-2"));

        panelB1.emit();
        assertTrue(panelB1.hasLoggedEvent("panel-B1", "event-1"));
        assertTrue(panelA1.hasNotLoggedEvent("panel-B1", "event-1"));
        assertTrue(panelA2.hasNotLoggedEvent("panel-B1", "event-1"));
        assertTrue(panelA3.hasNotLoggedEvent("panel-B1", "event-1"));
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        try
        {
            if (remotePluginA != null)
            {
                remotePluginA.stopAndUninstall();
            }
        }
        finally
        {
            if (remotePluginB != null)
            {
                remotePluginB.stopAndUninstall();
            }
        }
    }
}
