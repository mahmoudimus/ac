package it.jira.permission;

import com.atlassian.jira.pageobjects.pages.admin.GlobalPermissionsPage;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.util.TestUser;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TestGlobalPermission extends JiraWebDriverTestBase
{
    private static final String pluginKey = AddonTestUtils.randomAddOnKey();
    private static final String userGroup = "jira-users";

    private static ConnectRunner remotePlugin;
    private static final String permissionKey = "plugged-global-permission";
    private static final String permissionName = "Custom connect global permission anonymus allowed";
    private static final String description = "Custom connect global permission where anonymous are allowed";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.environmentData().getBaseUrl().toString(), pluginKey)
                .setAuthenticationToNone()
                .addModule(
                        "jiraGlobalPermissions",
                        GlobalPermissionModuleBean.newGlobalPermissionModuleBean()
                                .withKey(permissionKey)
                                .withName(new I18nProperty(permissionName, null))
                                .withDescription(new I18nProperty(description, null))
                                .withAnonymusAllowed(true)
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

    @Test
    public void pluggableGlobalPermissionShouldAppearOnTheGlobalPermissionList()
    {
        GlobalPermissionsPage globalPermissionsPage = loginAndVisit(new TestUser("admin"), GlobalPermissionsPage.class);
        List<GlobalPermissionsPage.GlobalPermissionRow> globalPermissions = globalPermissionsPage.getGlobalPermissions();
        assertThat(globalPermissions, Matchers.<GlobalPermissionsPage.GlobalPermissionRow>hasItem(Matchers.allOf(
                hasProperty("permissionName", is(permissionName)),
                hasProperty("secondaryText", is(description)),
                hasProperty("groupsAndUsers", Matchers.emptyIterable())
        )));
    }
}
