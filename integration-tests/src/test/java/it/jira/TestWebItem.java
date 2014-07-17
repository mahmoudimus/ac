package it.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
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
    private static final String ABSOLUTE_WEBITEM = "absolute-web-item";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules(
                        "webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("General Web Item", null))
                                .withKey(GENERAL_WEBITEM)
                                .withUrl("/web-item?issue_id=${issue.id}&project_key=${project.key}&pid=${project.id}")
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty("Absolute Web Item", null))
                                .withKey(ABSOLUTE_WEBITEM)
                                .withUrl(product.getProductInstance().getBaseUrl() + "/browse/${project.key}")
                                .withLocation("system.top.navigation.bar")
                                .withWeight(1)
                                .build()
                )
                .addRoute("/web-item", ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testWebItemWithProjectInContext() throws RemoteException
    {
        loginAsAdmin();

        product.visit(JiraViewProjectPage.class, project.getKey());

        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(runner.getAddon().getKey(), GENERAL_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be visible", webItem);
        assertTrue("Web item link should point to add-on base", webItem.getPath().startsWith(runner.getAddon().getBaseUrl()));

        webItem.click();

        assertEquals(project.getKey(), webItem.getFromQueryString("project_key"));
        assertEquals(project.getId(), webItem.getFromQueryString("pid"));
    }

    @Test
    public void testAbsoluteWebItemWithContext()
    {
        loginAsAdmin();

        product.visit(JiraViewProjectPage.class, project.getKey());

        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(runner.getAddon().getKey(), ABSOLUTE_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be visible", webItem);
        assertTrue("Web item link should point to product base", webItem.getPath().startsWith(product.getProductInstance().getBaseUrl()));

        webItem.click();

        assertThat(webItem.getPath(), endsWith(project.getKey()));
    }

}
