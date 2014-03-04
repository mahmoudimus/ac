package it.com.atlassian.plugin.connect.usermanagement.confluence;

import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.user.UserManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertTrue;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConfluenceSpaceAdminScopeTest extends ConfluenceAdminScopeTestBase
{
    private final SpaceManager spaceManager;

    public ConfluenceSpaceAdminScopeTest(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder,
            PermissionManager confluencePermissionManager, UserManager userManager, SpaceManager spaceManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, confluencePermissionManager, userManager);
        this.spaceManager = spaceManager;
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.SPACE_ADMIN;
    }

    @Override
    protected boolean shouldBeAdmin()
    {
        return false;
    }

    @Test
    public void addonIsMadeAdminOfExistingSpace() throws Exception
    {
        List<Space> allSpaces = spaceManager.getAllSpaces();

        List<String> spaceAdminErrors = Lists.newArrayList();

        for (Space space : allSpaces)
        {
            boolean canAdminister = confluencePermissionManager.hasPermission(getAddonUser(), Permission.ADMINISTER, space);
            if (!canAdminister)
            {
                spaceAdminErrors.add("Add-on user " + getAddonUserKey() + " should have administer permission for space " + space.getKey());
            }
        }

        assertTrue(StringUtils.join(spaceAdminErrors, '\n'), spaceAdminErrors.isEmpty());
    }

    @Test
    public void addonIsMadeAdminOfNewSpace() throws Exception
    {
        ConfluenceUser admin = FindUserHelper.getUserByUsername("admin");

        Space jediSpace = spaceManager.createSpace("JEDI", "Knights of the Old Republic", "It's a trap!", admin);

        boolean addonCanAdministerNewSpace = confluencePermissionManager.hasPermission(getAddonUser(), Permission.ADMINISTER, jediSpace);
        assertTrue("Add-on user " + getAddonUserKey() + " should have administer permission for space " + jediSpace.getKey(), addonCanAdministerNewSpace);
    }
}
