package it.com.atlassian.plugin.connect.usermanagement.confluence;

import javax.annotation.Nullable;

import com.atlassian.confluence.cache.ThreadLocalCache;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.ConfluenceUserImpl;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.user.UserManager;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConfluenceSpaceAdminScopeTest extends ConfluenceAdminScopeTestBase
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceSpaceAdminScopeTest.class);

    private static final String JEDI_SPACE_KEY = "JEDI" + System.currentTimeMillis();
    private final SpaceManager spaceManager;
    private final SpacePermissionManager spacePermissionManager;
    private Space jediSpace;

    public ConfluenceSpaceAdminScopeTest(TestPluginInstaller testPluginInstaller,
                                         JwtApplinkFinder jwtApplinkFinder,
                                         PermissionManager confluencePermissionManager,
                                         UserManager userManager,
                                         SpaceManager spaceManager,
                                         TestAuthenticator testAuthenticator,
                                         SpacePermissionManager spacePermissionManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, confluencePermissionManager, userManager, testAuthenticator);
        this.spaceManager = spaceManager;
        this.spacePermissionManager = spacePermissionManager;
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.SPACE_ADMIN;
    }

    @Override
    protected ScopeName getScopeOneDown()
    {
        return ScopeName.DELETE;
    }

    @Override
    protected boolean shouldBeAdmin()
    {
        return false;
    }

    @After
    public void cleanup() {
        if (jediSpace != null)
        {
            try
            {
                spaceManager.removeSpace(jediSpace);
                jediSpace = null;
            }
            catch (Exception e)
            {
                //couldn't delete the space for some reason, just ignore
            }
        }
    }

    @Test
    public void addonIsMadeAdminOfExistingSpace() throws Exception
    {
        List<Space> allSpaces = spaceManager.getAllSpaces();

        List<String> spaceAdminErrors = Lists.newArrayList();

        final ConfluenceUser addonUser = getAddonUser();

        for (Space space : allSpaces)
        {
            /*
             * Confluence caches some security stuff on thread local and due to a bug we need to blast it away before checking permission
             */
            ThreadLocalCache.flush();

            boolean canAdminister = spacePermissionManager.hasPermission(SpacePermission.ADMINISTER_SPACE_PERMISSION, space, addonUser);
            if (!canAdminister)
            {
                spaceAdminErrors.add("Add-on user " + getAddonUsername() + " should have administer permission for space " + space.getKey());
            }
        }

        assertTrue(StringUtils.join(spaceAdminErrors, '\n'), spaceAdminErrors.isEmpty());
    }

    @Test
    public void addonIsMadeAdminOfNewSpace() throws Exception
    {
        ConfluenceUser admin = FindUserHelper.getUserByUsername("admin");

        jediSpace = spaceManager.createSpace(JEDI_SPACE_KEY, "Knights of the Old Republic", "It's a trap!", admin);

        final ConfluenceUser addonUser = getAddonUser();

        /*
         * Confluence caches some security stuff on thread local and due to a bug we need to blast it away before checking permission
         */
        ThreadLocalCache.flush();

        boolean addonCanAdministerNewSpace = spacePermissionManager.hasPermission(SpacePermission.ADMINISTER_SPACE_PERMISSION, jediSpace, addonUser);
        assertTrue("Add-on user " + getAddonUsername() + " should have administer permission for space " + jediSpace.getKey(), addonCanAdministerNewSpace);
    }

    @Test
    public void isNotSpaceAdminAfterDowngrade() throws Exception
    {
        installLowerScopeAddon();
        assertEquals(false, isUserSpaceAdminOfAnySpace(getAddonUsername()));
    }


    private boolean isUserSpaceAdminOfAnySpace(String username)
    {
        // now flush the permissions cache so that it rebuilds to reflect new permission sets
        //
        // this is needed because Confluence's CachingSpacePermissionManager caches permissions in ThreadLocalCache
        // and doesn't realise when the permissions have changed
        //
        // the alternative is to flush the cache in the prod code, which may have unintended side-effects
        ThreadLocalCache.flush();

        final ConfluenceUser addonUser = getUser(username);

        /*
         * is an admin if admin on any of the spaces
         */
        List<Space> allSpaces = spaceManager.getAllSpaces();
        return Iterables.any(allSpaces, new Predicate<Space>()
        {
            @Override
            public boolean apply(@Nullable Space space)
            {
                final boolean hasPermission = spacePermissionManager.hasPermission(SpacePermission.ADMINISTER_SPACE_PERMISSION, space, addonUser);
                if (hasPermission)
                {
                    log.debug("***** user {} has space admin permission on space {}", new Object[] {addonUser, space});
                }
                return hasPermission;
            }
        });
    }
}
