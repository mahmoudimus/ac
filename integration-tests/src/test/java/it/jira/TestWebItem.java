package it.jira;


import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.RemoteWebItemModule;
import com.google.common.base.Optional;
import it.MyContextAwareWebPanelServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test of remote web items in JIRA.
 */
public class TestWebItem extends JiraWebDriverTestBase
{
    private static final String GENERAL_WEBITEM = "system-web-item";
    private static final String ABSOLUTE_WEB_ITEM = "absolute-web-item";

    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(RemoteWebItemModule.key(GENERAL_WEBITEM)
                        .name("AC General Web Item")
                        .section("system.top.navigation.bar")
                        .weight(1)
                        .link(RemoteWebItemModule.Link.link("/irwi?issue_id=${issue.id}&project_key=${project.key}&pid=${project.id}", false))
                        .resource(newServlet(new MyContextAwareWebPanelServlet())))
                .add(RemoteWebItemModule.key(ABSOLUTE_WEB_ITEM)
                        .name("Quick project link")
                        .section("system.top.navigation.bar")
                        .weight(1)
                        .link(RemoteWebItemModule.Link.link(product.getProductInstance().getBaseUrl() + "/browse/${project.key}", false)))
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
    public void testWebItemWithProjectInContext() throws RemoteException
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(GENERAL_WEBITEM, Optional.<String>absent());

        assertNotNull("Web item should be found", webItem);

        webItem.click();

        assertFalse("Web item link shouldn't be absolute", webItem.isPonitingToOldXmlInternalUrl());
        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
    }

    @Test
    public void testAbsoluteWebItemWithContext()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ABSOLUTE_WEB_ITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        webItem.click();

        assertTrue("Web item link should be absolute", webItem.isPonitingToOldXmlInternalUrl());
        assertThat(webItem.getPath(), endsWith(project.getKey()));
    }

}
