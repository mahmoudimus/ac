package it.com.atlassian.plugin.connect.usermanagement.confluence;

import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.assertTrue;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConfluenceSpaceAdminUserManagementTest
{
    private static final String ADMIN = "admin";
    private static String ADDON_KEY = "space-admin-addon";
    private static final String INSTALLED = "/installed";

    private final ConnectAddOnUserService connectAddOnUserService;
    private final SpaceManager spaceManager;
    private final PermissionManager permissionManager;
    private final TestPluginInstaller testPluginInstaller;

    @Rule
    public ErrorCollector spacePermissionErrorCollector = new ErrorCollector();

    private ConnectAddonBean spaceAdminAddon;

    public ConfluenceSpaceAdminUserManagementTest(ConnectAddOnUserService connectAddOnUserService,
            SpaceManager spaceManager, PermissionManager permissionManager, TestPluginInstaller testPluginInstaller)
    {
        this.connectAddOnUserService = connectAddOnUserService;
        this.spaceManager = spaceManager;
        this.permissionManager = permissionManager;
        this.testPluginInstaller = testPluginInstaller;
    }

    @Before
    public void setUp()
    {
        this.spaceAdminAddon = newConnectAddonBean()
                .withName("Confluence Space Admin Add-on")
                .withKey(ADDON_KEY)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withScopes(Sets.newHashSet(ScopeName.SPACE_ADMIN))
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
                .build();
    }

    @Test
    public void addonIsMadeAdminOfExistingSpace() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(spaceAdminAddon);

            String addonUserKey = connectAddOnUserService.getOrCreateUserKey(ADDON_KEY);
            ConfluenceUser addonUser = FindUserHelper.getUserByUsername(addonUserKey);

            List<Space> allSpaces = spaceManager.getAllSpaces();

            List<String> spaceAdminErrors = Lists.newArrayList();

            for (Space space : allSpaces)
            {
                boolean canAdminister = permissionManager.hasPermission(addonUser, Permission.ADMINISTER, space);
                if (!canAdminister)
                {
                    spaceAdminErrors.add("Add-on user " + addonUserKey + " should have administer permission for space " + space.getKey());
                }
            }

            assertTrue(StringUtils.join(spaceAdminErrors, '\n'), spaceAdminErrors.isEmpty());
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void addonIsMadeAdminOfNewSpace() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(spaceAdminAddon);

            String addonUserKey = connectAddOnUserService.getOrCreateUserKey(ADDON_KEY);
            ConfluenceUser addonUser = FindUserHelper.getUserByUsername(addonUserKey);

            ConfluenceUser admin = FindUserHelper.getUserByUsername(ADMIN);

            Space jediSpace = spaceManager.createSpace("JEDI", "Knights of the Old Republic", "It's a trap!", admin);

            boolean addonCanAdministerNewSpace = permissionManager.hasPermission(addonUser, Permission.ADMINISTER, jediSpace);
            assertTrue("Add-on user " + addonUserKey + " should have administer permission for space " + jediSpace.getKey(), addonCanAdministerNewSpace);
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }
}
