package it.capabilities.jira;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

import com.google.common.base.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.capabilities.CheckUsernameConditionServlet;
import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestJiraConditions extends JiraWebDriverTestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;
    private static final String ONLY_BETTY_WEBITEM = "only-betty";
    private static final String ONLY_BARNEY_WEBITEM = "only-barney";
    private static final String BETTY_AND_BARNEY_WEBITEM = "betty-and-barney";
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("Only Betty", ONLY_BETTY_WEBITEM))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build()
                                ,newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                        )
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("Betty And Barney", BETTY_AND_BARNEY_WEBITEM))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build()
                                , newCompositeConditionBean()
                                .withType(CompositeConditionType.OR)
                                .withConditions(
                                        newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                                        , newSingleConditionBean().withCondition("/onlyBarneyCondition").build()
                                ).build()
                        )
                        .build())
                .addCapability(newWebItemBean()
                        .withName(new I18nProperty("Admin Rights", ADMIN_RIGHTS_WEBITEM))
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withLink("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_admin").build()
                        )
                        .build())
                .addRoute("/onlyBarneyCondition", new CheckUsernameConditionServlet(BARNEY_USERNAME))
                .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet(BETTY_USERNAME))
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
    public void bettyCanSeeBettyWebItem()
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ONLY_BETTY_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeBettyWebItem()
    {
        loginAs(BARNEY_USERNAME, BARNEY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(ONLY_BETTY_WEBITEM));
    }
    
    @Test
    public void adminCannotSeeBettyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(ONLY_BETTY_WEBITEM));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem()
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(BETTY_AND_BARNEY_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem()
    {
        loginAs(BARNEY_USERNAME, BARNEY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(BETTY_AND_BARNEY_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(BETTY_AND_BARNEY_WEBITEM));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem()
    {
        loginAs(BETTY_USERNAME, BETTY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADMIN_RIGHTS_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem()
    {
        loginAs(BARNEY_USERNAME, BARNEY_USERNAME);

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(ADMIN_RIGHTS_WEBITEM));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(ADMIN_RIGHTS_WEBITEM, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

}
