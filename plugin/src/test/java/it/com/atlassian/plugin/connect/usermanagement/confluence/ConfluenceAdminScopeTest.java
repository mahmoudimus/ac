package it.com.atlassian.plugin.connect.usermanagement.confluence;

import java.io.IOException;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.user.UserManager;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceAdminScopeTest extends ConfluenceAdminScopeTestBase
{
    private final SpaceManager spaceManager;
    private final SpacePermissionManager spacePermissionManager;

    public ConfluenceAdminScopeTest(TestPluginInstaller testPluginInstaller,
                                    JwtApplinkFinder jwtApplinkFinder,
                                    PermissionManager confluencePermissionManager,
                                    UserManager userManager,
                                    TestAuthenticator testAuthenticator,
                                    SpaceManager spaceManager,
                                    SpacePermissionManager spacePermissionManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, confluencePermissionManager, userManager, testAuthenticator);
        this.spaceManager = spaceManager;
        this.spacePermissionManager = spacePermissionManager;
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.ADMIN;
    }

    @Override
    protected ScopeName getScopeOneDown()
    {
        return ScopeName.SPACE_ADMIN;
    }

    @Override
    protected ScopeName getScopeOneUp()
    {
        fail("doh refactor this as it doesn't make sense");
        return null;
    }

    @Override
    protected boolean shouldBeAdmin()
    {
        return true;
    }

    @Test
    public void isNotTopLevelAdminAfterDowngrade() throws Exception
    {
        installLowerScopeAddon();
        assertEquals(false, isUserTopLevelAdmin(getAddonUsername()));
    }

    @Test
    public void isSpaceAdminAfterDowngrade() throws IOException
    {
        installLowerScopeAddon();
        // TODO: dodgy. If we move this to the ConfluenceSpaceAdminScopeTest then the setup will already install the addon with space admin
        // so we'd have to then install as admin and finally install again as space admin which is dodgy too. Maybe it needs its own test
        ConfluenceSpaceAdminScopeTest.assertIsSpaceAdminOnAllSpaces(spaceManager, spacePermissionManager, getAddonUser());
    }

}
