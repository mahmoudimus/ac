package com.atlassian.plugin.connect.plugin.usermanagement.confluence;

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
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonAccessor;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
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
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("confluence-users", "users");

    private final PermissionManager confluencePermissionManager;
    private final SpacePermissionManager spacePermissionManager;
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;
    private final EventPublisher eventPublisher;
    private final ConnectAddonAccessor connectAddonAccessor;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public ConfluenceAddOnUserProvisioningService(PermissionManager confluencePermissionManager,
                                                  SpacePermissionManager spacePermissionManager,
                                                  SpaceManager spaceManager,
                                                  UserAccessor userAccessor,
                                                  UserManager userManager,
                                                  EventPublisher eventPublisher,
                                                  ConnectAddonAccessor connectAddonAccessor,
                                                  TransactionTemplate transactionTemplate)
    {
        this.confluencePermissionManager = confluencePermissionManager;
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
        this.eventPublisher = eventPublisher;
        this.connectAddonAccessor = connectAddonAccessor;
        this.transactionTemplate = transactionTemplate;
        eventPublisher.register(this);
    }

    @Override
    public void provisionAddonUserForScopes(final String username, final Set<ScopeName> previousScopes, final Set<ScopeName> newScopes)
    {
        transactionTemplate.execute(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                final ConfluenceUser confluenceAddonUser = getConfluenceUser(username);
                Set<ScopeName> normalizedPreviousScopes = ScopeName.normalize(previousScopes);
                Set<ScopeName> normalizedNewScopes = ScopeName.normalize(newScopes);

                // x to ADMIN scope transition
                if (ScopeName.containsAdmin(normalizedNewScopes))
                {
                    grantAddonUserGlobalAdmin(confluenceAddonUser);
                }
                // x to SPACE_ADMIN scope transition
                else if (ScopeName.isTransitionUpToSpaceAdmin(normalizedPreviousScopes, normalizedNewScopes))
                {
                    // add space admin to all spaces
                    grantAddonUserSpaceAdmin(confluenceAddonUser);
                }

                // ADMIN to x scope transition
                if (ScopeName.isTransitionDownFromAdmin(normalizedPreviousScopes, normalizedNewScopes))
                {
                    removeUserFromGlobalAdmins(confluenceAddonUser);
                }
                // SPACE_ADMIN to x scope transition
                else if (ScopeName.isTransitionDownFromSpaceAdmin(normalizedPreviousScopes, normalizedNewScopes))
                {
                    removeSpaceAdminPermissions(confluenceAddonUser);
                }

                return null;
            }
        });
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        return GROUPS;
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

        // we need to run this without permissions checking otherwise our attempt to modify permissions will be permissions-checked,
        // and will be accordingly rejected unless we tell Confluence that it's being performed by an arbitrarily selected admin user
        confluencePermissionManager.withExemption(new Runnable()
        {
            @Override
            public void run()
            {
                EditPermissionsAdministrator confluenceEditPermissionsAdministrator = confluencePermissionsAdministratorBuilder.buildEditGlobalPermissionAdministrator(null, asList(confluenceAddonUser.getName()), Collections.<String>emptyList());

                if (shouldBeAdmin)
                {
                    SpacePermission permission = SpacePermission.createUserSpacePermission(SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION, null, confluenceAddonUser);
                    confluenceEditPermissionsAdministrator.addPermission(permission);
                }
                else
                {
                    removePermission(confluenceEditPermissionsAdministrator, confluenceAddonUser, SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION);
                }
            }
        });
        // Confluence's CachingSpacePermissionManager caches permissions in ThreadLocalCache and doesn't realise when the permissions have changed.
        // This is Less Than Ideal.
        // Due to this bug all threads now have an outdated copy of this user's admin permission.
        // On a new request from the add-on or page-view by a user a new thread will be used, so they will be OK.
        // We could call ThreadLocalCache.flush() to fix up this thread but that may have unintended side-effects.
        // The test code in ConfluenceAdminScopeTestBase flushes the thread-local cache before checking results.
    }

    // because in Confluence you can't "remove permission CONFLUENCE_ADMINISTRATOR_PERMISSION": you have to remove the expact permission object instance (OMGWTFBBQ)
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
        if (!spacePermissionManager.hasPermission(ADMINISTER_SPACE_PERMISSION, space, confluenceAddonUser))
        {
            SpacePermission permission = new SpacePermission(ADMINISTER_SPACE_PERMISSION, space, null, confluenceAddonUser);
            spacePermissionManager.savePermission(permission);
        }
        else
        {
            log.info("Add-on user {} already has admin permission on space {}", confluenceAddonUser.getName(), space.getKey());
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
        Set<SpacePermission> allSpacePermissionsAssignedToAddonUser = Sets.newHashSet();
        for (SpacePermission spacePermission : space.getPermissions())
        {
            if (spacePermission.isUserPermission() &&
                    Objects.equal(spacePermission.getUserSubject(), confluenceAddonUser) &&
                    StringUtils.equals(spacePermission.getType(), SpacePermission.ADMINISTER_SPACE_PERMISSION))
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
            // TODO:*** Need to avoid duping the addon user naming scheme but don't want to create a circular dependency
            String username =  "addon_" + connectAddonBean.getKey();
            grantAddonUserSpaceAdmin(getConfluenceUser(username));
        }

    }

    private Iterable<ConnectAddonBean> fetchAddonsWithSpaceAdminScope()
    {
        return filter(connectAddonAccessor.fetchConnectAddons(), new Predicate<ConnectAddonBean>()
        {
            @Override
            public boolean apply(@Nullable ConnectAddonBean addon)
            {
                final Set<ScopeName> normalizedScopes = ScopeName.normalize(addon.getScopes());
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
