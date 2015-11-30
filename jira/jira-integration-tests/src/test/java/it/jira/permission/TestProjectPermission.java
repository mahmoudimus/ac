package it.jira.permission;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionCategory;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.jira.JiraTestBase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static it.jira.permission.PermissionJsonBean.PermissionType.PROJECT;
import static it.jira.permission.PermissionJsonBeanMatcher.isPermission;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestProjectPermission extends JiraTestBase
{
    private static final String pluginKey = AddonTestUtils.randomAddOnKey();
    private static final String permissionKey = "plugged-project-permission";
    private static final String fullPermissionKey = ModuleKeyUtils.addonAndModuleKey(pluginKey, permissionKey);
    private static final String permissionName = "Custom connect project permission";
    private static final String description = "Custom connect global permission";
    private static final ProjectPermissionCategory permissionCategory = ProjectPermissionCategory.ISSUES;
    private static final String projectKey = "TEST";

    private static MyPermissionRestClient myPermissionRestClient;
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        myPermissionRestClient = new MyPermissionRestClient(product);
        remotePlugin = new ConnectRunner(product.environmentData().getBaseUrl().toString(), pluginKey)
                .setAuthenticationToNone()
                .addModule(
                        "jiraProjectPermissions",
                        ProjectPermissionModuleBean.newProjectPermissionModuleBean()
                                .withKey(permissionKey)
                                .withName(new I18nProperty(permissionName, null))
                                .withDescription(new I18nProperty(description, null))
                                .withCategory(permissionCategory)
                                .withConditions(SingleConditionBean.newSingleConditionBean()
                                        .withCondition("voting_enabled")
                                        .build())
                                .build()
                )
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

    @Before
    public void setup()
    {
        product.backdoor().project().addProject("Test project", projectKey, "admin");
    }

    @After
    public void tearDown()
    {
        product.backdoor().project().deleteProject(projectKey);
    }

    @Test
    public void pluggableProjectPermissionShouldDisplayOnTheProjectPermission() throws Exception
    {
        Map<String, PermissionJsonBean> myPermissions = myPermissionRestClient.getMyPermissions();

        PermissionJsonBean permission = myPermissions.get(fullPermissionKey);
        assertThat(permission, isPermission(fullPermissionKey, PROJECT, permissionName, description));
    }

    @Test
    public void pluggableProjectPermissionShouldNotDisplayIfConditionsAreNotFulfilled() throws Exception
    {
        product.backdoor().applicationProperties().setOption(APKeys.JIRA_OPTION_VOTING, false);

        Map<String, PermissionJsonBean> myPermissions = myPermissionRestClient.getMyPermissions();

        PermissionJsonBean permission = myPermissions.get(fullPermissionKey);
        assertThat(permission, nullValue());

        product.backdoor().applicationProperties().setOption(APKeys.JIRA_OPTION_VOTING, true);
    }
}
