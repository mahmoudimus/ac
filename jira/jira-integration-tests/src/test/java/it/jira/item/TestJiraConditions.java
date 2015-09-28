package it.jira.item;

import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.jira.JiraWebDriverTestBase;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestJiraConditions extends JiraWebDriverTestBase
{

    private static ConnectRunner runner;

    private static String onlyBettyWebItem;
    private static String bettyAndBarneyWebitem;
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";

    private static String onlyBettyConditionUrl;
    private static String onlyBarneyConditionUrl;

    private static TestUser betty;
    private static TestUser barney;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        betty = testUserFactory.admin();
        barney = testUserFactory.basicUser();

        onlyBettyWebItem = "only-" + betty.getDisplayName();
        bettyAndBarneyWebitem = betty.getDisplayName() + "-and-" + barney.getDisplayName();
        onlyBettyConditionUrl = "/only" + betty.getDisplayName() + "Condition";
        onlyBarneyConditionUrl = "/only" + barney.getDisplayName() + "Condition";

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                    newWebItemBean()
                        .withName(new I18nProperty("Only Betty", onlyBettyWebItem))
                        .withKey(onlyBettyWebItem)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                newSingleConditionBean().withCondition(onlyBettyConditionUrl).build()
                        )
                        .build(),
                    newWebItemBean()
                        .withName(new I18nProperty("Betty And Barney", bettyAndBarneyWebitem))
                        .withKey(bettyAndBarneyWebitem)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                newCompositeConditionBean()
                                    .withType(CompositeConditionType.OR)
                                    .withConditions(
                                            newSingleConditionBean().withCondition(onlyBettyConditionUrl).build(),
                                            newSingleConditionBean().withCondition(onlyBarneyConditionUrl).build()
                                    ).build()
                        )
                        .build(),
                    newWebItemBean()
                        .withName(new I18nProperty("Admin Rights", ADMIN_RIGHTS_WEBITEM))
                        .withKey(ADMIN_RIGHTS_WEBITEM)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                            newSingleConditionBean().withCondition("user_is_admin").build()
                        )
                        .build())
                .addRoute(onlyBarneyConditionUrl, new CheckUsernameConditionServlet(barney))
                .addRoute(onlyBettyConditionUrl, new CheckUsernameConditionServlet(betty))
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
    public void bettyCanSeeBettyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(betty, JiraViewProjectPage.class, project.getKey());
        assertNotNull("Web item should be found", viewProjectPage.findWebItem(getModuleKey(onlyBettyWebItem), Optional.<String>absent()));
    }

    @Test
    public void barneyCannotSeeBettyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(barney, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(onlyBettyWebItem)));
    }

    @Test
    public void adminCannotSeeBettyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(testUserFactory.admin(), JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(onlyBettyWebItem)));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(betty, JiraViewProjectPage.class, project.getKey());
        assertNotNull("Web item should be found", viewProjectPage.findWebItem(getModuleKey(bettyAndBarneyWebitem), Optional.<String>absent()));
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(barney, JiraViewProjectPage.class, project.getKey());
        assertNotNull("Web item should be found", viewProjectPage.findWebItem(getModuleKey(bettyAndBarneyWebitem), Optional.<String>absent()));
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(testUserFactory.admin(), JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(bettyAndBarneyWebitem)));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(betty, JiraViewProjectPage.class, project.getKey());
        assertNotNull("Web item should be found", viewProjectPage.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.<String>absent()));
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(barney, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ADMIN_RIGHTS_WEBITEM)));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(testUserFactory.admin(), JiraViewProjectPage.class, project.getKey());
        assertNotNull("Web item should be found", viewProjectPage.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.<String>absent()));
    }

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(runner.getAddon().getKey(), module);
    }    
}
