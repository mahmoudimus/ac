package com.atlassian.plugin.connect.confluence.usermanagement;

import com.atlassian.confluence.cache.ThreadLocalCache;
import com.atlassian.confluence.event.events.space.SpaceCreateEvent;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SetSpacePermissionChecker;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.security.administrators.EditPermissionsAdministrator;
import com.atlassian.confluence.security.administrators.PermissionsAdministratorBuilder;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATEEDIT_PAGE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.EDITBLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_BLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_PAGE_PERMISSION;
import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;

@SuppressWarnings ("unused")
@ConfluenceComponent
@ExportAsDevService
public class ConfluenceAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceAddOnUserProvisioningService.class);

    // As reported by Sam Day, without the "confluence-users" group the add-on user can't
    // even get the page summary of a page that is open to anonymous access.
    private static final ImmutableSet<String> DEFAULT_GROUPS_ALWAYS_EXPECTED = ImmutableSet.of("_licensed-confluence");
    private static final ImmutableSet<String> DEFAULT_GROUPS_ONE_OR_MORE_EXPECTED = ImmutableSet.of("confluence-users", "users");


    private static final ImmutableSet<String> SPACE_ADMIN_PERMISSIONS = ImmutableSet.of(
        // WRITE
        CREATEEDIT_PAGE_PERMISSION, CREATE_ATTACHMENT_PERMISSION, COMMENT_PERMISSION, EDITBLOG_PERMISSION,

        // DELETE
        REMOVE_PAGE_PERMISSION, REMOVE_ATTACHMENT_PERMISSION, REMOVE_COMMENT_PERMISSION,
        REMOVE_BLOG_PERMISSION,

        // SPACE_ADMIN
        ADMINISTER_SPACE_PERMISSION);


    private final PermissionManager confluencePermissionManager;
    private final SpacePermissionManager spacePermissionManager;
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;
    private final EventPublisher eventPublisher;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public ConfluenceAddOnUserProvisioningService(PermissionManager confluencePermissionManager,
                                                  SpacePermissionManager spacePermissionManager,
                                                  SpaceManager spaceManager,
                                                  UserAccessor userAccessor,
                                                  UserManager userManager,
                                                  EventPublisher eventPublisher,
                                                  ConnectAddonRegistry connectAddonRegistry, TransactionTemplate transactionTemplate)
    {
        this.confluencePermissionManager = confluencePermissionManager;
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
        this.eventPublisher = eventPublisher;
        this.connectAddonRegistry = connectAddonRegistry;
        this.transactionTemplate = transactionTemplate;
        eventPublisher.register(this);
    }

    @Override
    public void provisionAddonUserForScopes(final String username, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes)
    {
        // Required for temporary permissions exemptions, because unless you call init() in the current thread at least once then you
        // can't use the thread-local cache, and we need to use it to set an exemption on "can the current user change this permission"
        // checks. During installs from the GUI this code will be called inside a UPM worker thread on which init() has never been called
        // (because why would UPM need to know about some obscure low-level detail of Confluence's memory management?).
        // Richard Atkins knows more about this and is working on Confluence changes that will make these low level calls unnecessary
        // in Connect code.
        // The init() call is outside of the try..finally block so that we don't dispose() of the cache if we fail to initialize it.
        // NOTE: it is safe to call init() multiple times as the second and subsequent calls do nothing.
        ThreadLocalCache.init();

        try
        {
            transactionTemplate.execute(new TransactionCallback<Void>()
            {
                @Override
                public Void doInTransaction()
                {
                    // We need to run this without permissions checking otherwise our attempt to modify permissions will be permissions-checked,
                    // and some will be accordingly rejected (particularly if this is running anonymously on a task thread, as is the case
                    // when the UPM auto-updates Connect add-ons).
                    // An even less palatable alternative is that we impersonate an arbitrary admin user.
                    confluencePermissionManager.withExemption(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            provisionAddonUserForScopeInTransaction(username, previousScopes, newScopes);
                        }
                    });

                    return null;
                }
            });
        }
        finally
        {
            // Now we need to dispose of the thread-local cache to free memory and remove the thread-local exemption on checking the current back-end user's
            // permissions. Unfortunately, we don't have a way of checking whether or not the cache was initialized by code in this method or in previous code.
            // Ideally, we would dispose() of the cache only if it was us that caused it to init().
            // Currently, it's always code in this method because the install is happening inside UPM worker threads on which init() has not been called.
            ThreadLocalCache.dispose();

            // NOTE: Confluence's CachingSpacePermissionManager caches permissions in the ThreadLocalCache and doesn't realise when the permissions have changed.
            // Due to this bug all threads with initialized thread-local caches now have outdated copies of this user's permissions.
            // On a new request from the add-on or page-view by a user a new thread will be used, so they will be OK.
            // The test code in ConfluenceAdminScopeTestBase flushes the thread-local cache before checking results, because it runs everything in the same thread.
        }
    }

    private void provisionAddonUserForScopeInTransaction(String username, Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        final ConfluenceUser confluenceAddonUser = getConfluenceUser(username);

        // After a manual re-install of the add-on, there are no previous known scopes, but there could still be
        // an existing permission setup from the previous installation that needs to be removed.
        boolean removeExistingPermissionSetup = previousScopes.isEmpty();

        // ADMIN to x scope transition
        if (removeExistingPermissionSetup || ScopeUtil.isTransitionDownFromAdmin(previousScopes, newScopes))
        {
            removeUserFromGlobalAdmins(confluenceAddonUser);
        }
        // SPACE_ADMIN to <= READ scope transition
        if (removeExistingPermissionSetup || ScopeUtil.isTransitionDownToReadOrLess(previousScopes, newScopes))
        {
            removeSpaceAdminPermissions(confluenceAddonUser);
        }
        // x to ADMIN scope transition
        if (ScopeUtil.isTransitionUpToAdmin(previousScopes, newScopes))
        {
            grantAddonUserGlobalAdmin(confluenceAddonUser);
        }

        // x to SPACE_ADMIN scope transition. Note: SPACE_ADMIN is given for all: READ > scopes < ADMIN
        if (ScopeUtil.isTransitionUpFromReadOrLess(previousScopes, newScopes))
        {
            // add space admin to all spaces
            grantAddonUserSpaceAdmin(confluenceAddonUser);
        }
    }
    
    @Override
    public Set<String> getDefaultProductGroupsAlwaysExpected()
    {
        return DEFAULT_GROUPS_ALWAYS_EXPECTED;
    }

    @Override
    public Set<String> getDefaultProductGroupsOneOrMoreExpected()
    {
        return DEFAULT_GROUPS_ONE_OR_MORE_EXPECTED;
    }

    private ConfluenceUser getConfluenceUser(String username)
    {
        UserProfile userProfile = userManager.getUserProfile(username);

        if (userProfile == null)
        {
            throw new IllegalStateException("User for user key " + username + " does not exist");
        }

        return userAccessor.getExistingUserByKey(userProfile.getUserKey());
    }

    private void grantAddonUserGlobalAdmin(final ConfluenceUser confluenceAddonUser)
    {
        setGlobalAdmin(confluenceAddonUser, true);
    }

    private void removeUserFromGlobalAdmins(ConfluenceUser confluenceAddonUser)
    {
        setGlobalAdmin(confluenceAddonUser, false);
    }

    private void setGlobalAdmin(final ConfluenceUser confluenceAddonUser, final boolean shouldBeAdmin)
    {
        // injecting as a dependency results in an instance with null data members - perhaps there are multiple instances or perhaps it is initialized after us
        final PermissionsAdministratorBuilder confluencePermissionsAdministratorBuilder = ComponentLocator.getComponent(PermissionsAdministratorBuilder.class, "permissionsAdministratorBuilder");
        EditPermissionsAdministrator confluenceEditPermissionsAdministrator = confluencePermissionsAdministratorBuilder.buildEditGlobalPermissionAdministrator(null, asList(confluenceAddonUser.getName()), Collections.<String>emptyList());

        if (shouldBeAdmin)
        {
            log.info("Making user '{}' a Confluence administrator.", confluenceAddonUser.getName());
            SpacePermission permission = SpacePermission.createUserSpacePermission(SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION, null, confluenceAddonUser);
            confluenceEditPermissionsAdministrator.addPermission(permission);
        }
        else
        {
            log.info("Removing Confluence administrator access from user '{}'.", confluenceAddonUser.getName());
            removePermission(confluenceEditPermissionsAdministrator, confluenceAddonUser, SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION);
        }
    }

    // because in Confluence you can't "remove permission CONFLUENCE_ADMINISTRATOR_PERMISSION": you have to remove the exact permission object instance (OMGWTFBBQ)
    private void removePermission(EditPermissionsAdministrator confluenceEditPermissionsAdministrator, ConfluenceUser confluenceAddonUser, String permissionType)
    {
        SetSpacePermissionChecker setSpacePermissionChecker = ComponentLocator.getComponent(SetSpacePermissionChecker.class, "setSpacePermissionChecker");
        List<SpacePermission> permissions = spacePermissionManager.getGlobalPermissions(permissionType);
        boolean found = false;

        for (SpacePermission permission : permissions)
        {
            if (null != permission && null != permission.getUserSubject() && null != permission.getUserSubject().getKey() &&
                permission.getUserSubject().getKey().getStringValue().equals(confluenceAddonUser.getKey().getStringValue()))
            {
                log.info("Removing Confluence permission '{}' from user '{}'.", permissionType, confluenceAddonUser.getName());
                confluenceEditPermissionsAdministrator.removePermission(permission);
                found = true;
                break;
            }
        }

        if (!found)
        {
            log.warn("Did not remove Confluence permission '{}' from user '{}' because it did not exist.", permissionType, confluenceAddonUser.getName());
        }
    }

    private void grantAddonUserSpaceAdmin(ConfluenceUser confluenceAddonUser)
    {
        final List<Space> spaces = spaceManager.getAllSpaces();
        for (Space space : spaces)
        {
            grantAddonUserAdminToSpace(space, confluenceAddonUser);
        }
    }

    private void grantAddonUserAdminToSpace(Space space, ConfluenceUser confluenceAddonUser)
    {
        for (String permission : SPACE_ADMIN_PERMISSIONS)
        {
            grantAddonUserPermissionToSpace(permission, space, confluenceAddonUser);
        }
    }

    private void grantAddonUserPermissionToSpace(String permissionName, Space space, ConfluenceUser confluenceAddonUser)
    {
        if (!spacePermissionManager.hasPermissionNoExemptions(permissionName, space, confluenceAddonUser))
        {
            SpacePermission permission = new SpacePermission(permissionName, space, null, confluenceAddonUser);
            spacePermissionManager.savePermission(permission);
        }
        else
        {
            log.info("Add-on user {} already has {} permission on space {}", new Object[] {
                    confluenceAddonUser.getName(), permissionName, space.getKey()});
        }
    }

    private void removeSpaceAdminPermissions(ConfluenceUser confluenceAddonUser)
    {
        final List<Space> spaces = spaceManager.getAllSpaces();
        for (Space space : spaces)
        {
            removeAddonUserAdminFromSpace(space, confluenceAddonUser);
        }
    }

    private void removeAddonUserAdminFromSpace(Space space, ConfluenceUser confluenceAddonUser)
    {
        for (String permission : SPACE_ADMIN_PERMISSIONS)
        {
            removeAddonUserPermissionFromSpace(permission, space, confluenceAddonUser);
        }
    }

    private void removeAddonUserPermissionFromSpace(String permissionName, Space space, ConfluenceUser confluenceAddonUser)
    {
        Set<SpacePermission> allSpacePermissionsAssignedToAddonUser = Sets.newHashSet();
        for (SpacePermission spacePermission : space.getPermissions())
        {
            if (spacePermission.isUserPermission() &&
                    Objects.equal(spacePermission.getUserSubject(), confluenceAddonUser) &&
                    StringUtils.equals(spacePermission.getType(), permissionName))
            {
                allSpacePermissionsAssignedToAddonUser.add(spacePermission);
            }
        }

        for (SpacePermission spacePermission : allSpacePermissionsAssignedToAddonUser)
        {
            spacePermissionManager.removePermission(spacePermission);
        }
    }

    @EventListener
    public void spaceCreated(SpaceCreateEvent spaceCreateEvent)
    {
        final Iterable<ConnectAddonBean> connectAddonBeans = fetchAddonsWithSpaceAdminScope();
        for (ConnectAddonBean connectAddonBean : connectAddonBeans)
        {
            String username =  ConnectAddOnUserUtil.usernameForAddon(connectAddonBean.getKey());
            grantAddonUserSpaceAdmin(getConfluenceUser(username));
        }

    }

    private Iterable<ConnectAddonBean> fetchAddonsWithSpaceAdminScope()
    {
        return filter(connectAddonRegistry.getAllAddonBeans(), new Predicate<ConnectAddonBean>()
        {
            @Override
            public boolean apply(@Nullable ConnectAddonBean addon)
            {
                final Set<ScopeName> normalizedScopes = ScopeUtil.normalize(addon.getScopes());
                return (normalizedScopes.contains(ScopeName.SPACE_ADMIN) && !normalizedScopes.contains(ScopeName.ADMIN));
            }
        });
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }


}
