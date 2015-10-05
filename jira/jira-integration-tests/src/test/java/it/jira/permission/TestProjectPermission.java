package it.jira.permission;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.pageobjects.project.permissions.Permission;
import com.atlassian.jira.pageobjects.project.permissions.PermissionGroup;
import com.atlassian.jira.pageobjects.project.permissions.ProjectPermissionPageTab;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionCategory;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.util.TestUser;
import org.elasticsearch.common.base.Predicate;
import org.elasticsearch.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestProjectPermission extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String userGroup = "jira-users";
    private static final String permissionKey = "plugged-project-permission";
    private static final String permissionName = "Custom connect project permission";
    private static final String description = "Custom connect global permission";
    public static final ProjectPermissionCategory PERMISSION_CATEGORY = ProjectPermissionCategory.ISSUES;
    public static final String PERMISSION_CATEGORY_NAME = "Issue Permissions";
    public static final String projectKey = "TEST";

    private static ConnectRunner remotePlugin;
    private static JSONObject property;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        property = new JSONObject().put("value", "true");
        remotePlugin = new ConnectRunner(product.environmentData().getBaseUrl().toString(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "jiraProjectPermissions",
                        ProjectPermissionModuleBean.newProjectPermissionModuleBean()
                                .withKey(permissionKey)
                                .withName(new I18nProperty(permissionName, null))
                                .withDescription(new I18nProperty(description, null))
                                .withCategory(PERMISSION_CATEGORY)
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
    public void pluggableProjectPermissionShouldDisplayOnTheProjectPermission()
    {
        ProjectPermissionPageTab projectPermissionPageTab = goToProjectPermissionPage();
        Permission permission = projectPermissionPageTab.getPermissionByName(permissionName);
        assertThat(permission, notNullValue());
    }

    @Test
    public void pluggableProjectPermissionShouldAppearInProperCategory()
    {
        ProjectPermissionPageTab projectPermissionPageTab = goToProjectPermissionPage();
        List<PermissionGroup> permissionGroups = projectPermissionPageTab.getPermissionGroups();
        PermissionGroup issueGroup = Iterables.find(permissionGroups, new Predicate<PermissionGroup>()
        {
            @Override
            public boolean apply(final PermissionGroup permissionGroup)
            {
                return permissionGroup.getName().equals(PERMISSION_CATEGORY_NAME);
            }
        });

        assertThat(issueGroup.getPermissions(), Matchers.<Permission>hasItem(
                Matchers.allOf(
                        hasProperty("name", is(permissionName)),
                        hasProperty("description", is(description)),
                        hasProperty("entities", empty())
                )
        ));
    }

    @Test
    public void pluggableProjectPermissionShouldNotDisplayIfConditionsAreNotFulfilled() throws JSONException
    {
        product.backdoor().applicationProperties().setOption(APKeys.JIRA_OPTION_VOTING, false);

        ProjectPermissionPageTab projectPermissionPageTab = goToProjectPermissionPage();
        Permission permission = projectPermissionPageTab.getPermissionByName(permissionName);

        assertThat(permission, nullValue());

        product.backdoor().applicationProperties().setOption(APKeys.JIRA_OPTION_VOTING, true);
    }

    private ProjectPermissionPageTab goToProjectPermissionPage()
    {
        return loginAndVisit(new TestUser("admin"), ProjectPermissionPageTab.class, projectKey);
    }
}
