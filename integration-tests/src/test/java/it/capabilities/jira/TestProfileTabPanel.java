package it.capabilities.jira;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.InsufficientPermissionsViewProfileTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.matcher.IsNotBlank.isNotBlank;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test of remote profile tab panel in JIRA
 */
public class TestProfileTabPanel extends ConnectWebDriverTestBase
{
    public static final String RAW_MODULE_KEY = "profile-tab-panel";
    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModule("jiraProfileTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty("Profile Tab Panel", null))
                                .withKey(RAW_MODULE_KEY)
                                .withUrl("/myProfileAddon")
                                .withWeight(1234)
                                .withConditions(
                                        toggleableConditionBean(),
                                        newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                                "?pUserKey={profileUser.key}&pUserName={profileUser.name}").build())
                                .build())
                .addRoute("/myProfileAddon", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
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

    @Test
    public void testProfileTabPanel() throws RemoteException
    {
        String moduleKey = addonAndModuleKey(remotePlugin.getAddon().getKey(),RAW_MODULE_KEY);
        loginAsAdmin();
        product.visit(ViewProfilePage.class);
        LinkedRemoteContent tabPanel = connectPageOperations.findTabPanel("up_" + moduleKey + "_a",
                Option.<String>none(),moduleKey);
        RemotePluginEmbeddedTestPage remotePage = tabPanel.click();
        assertThat(remotePage.isLoaded(), equalTo(true));
        assertThat(remotePage.getMessage(), equalTo("Success"));

        Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("pUserName", "admin"));
        assertThat(conditionRequestParams, hasEntry(is("pUserKey"), isNotBlank()));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws RemoteException
    {
        loginAsAdmin();

        remotePlugin.setToggleableConditionShouldDisplay(false);
        JiraViewProfilePage profilePage = product.visit(JiraViewProfilePage.class);

        InsufficientPermissionsViewProfileTab profileTab = profilePage.openTab(InsufficientPermissionsViewProfileTab.class,remotePlugin.getAddon().getKey(),RAW_MODULE_KEY);
        assertThat(profileTab.getErrorMessage(),
                containsString("You do not have the correct permissions to view the page Profile Tab Panel."));
    }


}
