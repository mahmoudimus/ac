package it.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteXdmEventPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import hudson.plugins.jira.soap.RemoteIssue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestXdmEvents extends JiraWebDriverTestBase
{
    private static ConnectRunner remotePluginA;
    private static ConnectRunner remotePluginB;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePluginA = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webPanels",
                        newWebPanelBean()
                                .withKey("xdm-events-a1")
                                .withName(new I18nProperty("XDM Events Panel A1", null))
                                .withLocation("atl.jira.view.issue.right.context")
                                .withUrl("/xdmEventsPanelA1")
                                .build(),
                        newWebPanelBean()
                                .withKey("xdm-events-a2")
                                .withName(new I18nProperty("XDM Events Panel A2", null))
                                .withLocation("atl.jira.view.issue.right.context")
                                .withUrl("/xdmEventsPanelA2")
                                .build(),
                        newWebPanelBean()
                                .withKey("xdm-events-a3")
                                .withName(new I18nProperty("XDM Events Panel A3", null))
                                .withLocation("atl.jira.view.issue.right.context")
                                .withUrl("/xdmEventsPanelA3")
                                .build())
                .addRoute("/xdmEventsPanelA1", newServlet(new XdmEventsPanelServlet("A1")))
                .addRoute("/xdmEventsPanelA2", newServlet(new XdmEventsPanelServlet("A2")))
                .addRoute("/xdmEventsPanelA3", newServlet(new XdmEventsPanelServlet("A3")))
                .start();

        remotePluginB = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webPanels",
                        newWebPanelBean()
                                .withKey("xdm-events-b1")
                                .withName(new I18nProperty("XDM Events Panel B1", null))
                                .withLocation("atl.jira.view.issue.right.context")
                                .withUrl("/xdmEventsPanelB1")
                                .build())
                .addRoute("/xdmEventsPanelB1", newServlet(new XdmEventsPanelServlet("B1")))
                .start();
    }

    @Test
    public void testXdmEvents() throws Exception
    {
        loginAsAdmin();

        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());

        RemoteXdmEventPanel panelA1 = viewIssuePage.findXdmEventPanel(remotePluginA.getAddon().getKey(), "xdm-events-a1");
        RemoteXdmEventPanel panelA2 = viewIssuePage.findXdmEventPanel(remotePluginA.getAddon().getKey(), "xdm-events-a2");
        RemoteXdmEventPanel panelA3 = viewIssuePage.findXdmEventPanel(remotePluginA.getAddon().getKey(), "xdm-events-a3");
        RemoteXdmEventPanel panelB1 = viewIssuePage.findXdmEventPanel(remotePluginB.getAddon().getKey(), "xdm-events-b1");

        assertEquals(panelA1.getModuleId(), "A1");
        assertEquals(panelA2.getModuleId(), "A2");
        assertEquals(panelA3.getModuleId(), "A3");
        assertEquals(panelB1.getModuleId(), "B1");

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
