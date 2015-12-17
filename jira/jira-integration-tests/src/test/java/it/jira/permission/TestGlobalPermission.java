package it.jira.permission;

import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.jira.JiraTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static it.jira.permission.PermissionJsonBean.PermissionType.GLOBAL;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestGlobalPermission extends JiraTestBase
{
    private static final String pluginKey = AddonTestUtils.randomAddonKey();

    private static final String permissionKey = "plugged-global-permission";
    private static final String fullPermissionKey = ModuleKeyUtils.addonAndModuleKey(pluginKey, permissionKey);
    private static final String permissionName = "Custom connect global permission anonymus allowed";
    private static final String description = "Custom connect global permission where anonymous are allowed";
    private static MyPermissionRestClient myPermissionRestClient;
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        myPermissionRestClient = new MyPermissionRestClient(product);
        remotePlugin = new ConnectRunner(product.environmentData().getBaseUrl().toString(), pluginKey)
                .setAuthenticationToNone()
                .addModule(
                        "jiraGlobalPermissions",
                        GlobalPermissionModuleBean.newGlobalPermissionModuleBean()
                                .withKey(permissionKey)
                                .withName(new I18nProperty(permissionName, null))
                                .withDescription(new I18nProperty(description, null))
                                .withAnonymousAllowed(true)
                                .build()
                )
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void pluggableGlobalPermissionShouldAppearOnTheGlobalPermissionList() throws Exception
    {
        Map<String, PermissionJsonBean> myPermissions = myPermissionRestClient.getMyPermissions();

        PermissionJsonBean customPermission = myPermissions.get(fullPermissionKey);
        assertThat(customPermission, PermissionJsonBeanMatcher.isPermission(fullPermissionKey, GLOBAL, permissionName, description));
    }
}
