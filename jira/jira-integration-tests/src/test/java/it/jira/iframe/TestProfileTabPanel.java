package it.jira.iframe;

import com.atlassian.connect.test.jira.pageobjects.InsufficientPermissionsViewProfileTab;
import com.atlassian.connect.test.jira.pageobjects.JiraViewProfilePage;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Optional;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.common.matcher.IsNotBlank.isNotBlank;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test of remote profile tab panel in JIRA
 */
public class TestProfileTabPanel extends JiraWebDriverTestBase {
    private static final String RAW_MODULE_KEY = "profile-tab-panel";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
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
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testProfileTabPanel() throws RemoteException {
        TestUser user = testUserFactory.basicUser();
        loginAndVisit(user, ViewProfilePage.class);
        String moduleKey = addonAndModuleKey(remotePlugin.getAddon().getKey(), RAW_MODULE_KEY);
        LinkedRemoteContent tabPanel = connectPageOperations.findTabPanel("up_" + moduleKey + "_a",
                Optional.<String>empty(), moduleKey);
        ConnectAddonEmbeddedTestPage remotePage = tabPanel.click();
        assertThat(remotePage.getMessage(), equalTo("Success"));

        Map<String, String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("pUserName", user.getUsername()));
        assertThat(conditionRequestParams, hasEntry(is("pUserKey"), isNotBlank()));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws RemoteException {
        login(testUserFactory.basicUser());

        remotePlugin.setToggleableConditionShouldDisplay(false);
        JiraViewProfilePage profilePage = product.visit(JiraViewProfilePage.class);

        InsufficientPermissionsViewProfileTab profileTab = profilePage.openTab(InsufficientPermissionsViewProfileTab.class, remotePlugin.getAddon().getKey(), RAW_MODULE_KEY);
        assertThat(profileTab.getErrorMessage(),
                containsString("You do not have the correct permissions to view the page Profile Tab Panel."));
    }


}
