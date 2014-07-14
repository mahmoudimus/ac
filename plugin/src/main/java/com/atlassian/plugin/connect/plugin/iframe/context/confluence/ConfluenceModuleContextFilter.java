package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.AbstractModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.PermissionCheck;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceModuleContextFilter extends AbstractModuleContextFilter<ConfluenceUser>
{
    public static final String PAGE_ID          = "page.id";
    public static final String PAGE_VERSION     = "page.version";
    public static final String PAGE_TYPE        = "page.type";
    public static final String CONTENT_ID       = "content.id";
    public static final String CONTENT_VERSION  = "content.version";
    public static final String CONTENT_TYPE     = "content.type";
    public static final String CONTENT_PLUGIN   = "content.plugin";
    public static final String SPACE_ID         = "space.id";
    public static final String SPACE_KEY        = "space.key";

    private final PermissionManager permissionManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;
    private final SpaceManager spaceManager;
    private final PageManager pageManager;
    private final ContentEntityManager contentEntityManager;

    private final Iterable<PermissionCheck<ConfluenceUser>> permissionChecks;

    @Autowired
    public ConfluenceModuleContextFilter(PermissionManager permissionManager, UserAccessor userAccessor,
            UserManager userManager, SpaceManager spaceManager, PageManager pageManager,
            @Qualifier("contentEntityManager") ContentEntityManager contentEntityManager)
    {
        this.permissionManager = permissionManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
        this.contentEntityManager = contentEntityManager;
        permissionChecks = constructPermissionChecks();
    }

    @Override
    protected ConfluenceUser getCurrentUser()
    {
        UserKey userKey = userManager.getRemoteUserKey();
        return userKey == null ? null : userAccessor.getExistingUserByKey(userKey);
    }

    @Override
    protected Iterable<PermissionCheck<ConfluenceUser>> getPermissionChecks()
    {
        return permissionChecks;
    }

    private Iterable<PermissionCheck<ConfluenceUser>> constructPermissionChecks()
    {
        return ImmutableList.of(
            new PermissionCheck<ConfluenceUser>()
            {
                @Override
                public String getParameterName()
                {
                    return SPACE_KEY;
                }

                @Override
                public boolean hasPermission(final String spaceKey, final ConfluenceUser user)
                {
                    Space space = spaceManager.getSpace(spaceKey);
                    return space != null && permissionManager.hasPermission(user, Permission.VIEW, space);
                }
            },
            new PermissionCheck.LongValue<ConfluenceUser>()
            {
                @Override
                public String getParameterName()
                {
                    return SPACE_ID;
                }

                @Override
                public boolean hasPermission(final long spaceId, final ConfluenceUser user)
                {
                    Space space = spaceManager.getSpace(spaceId);
                    return space != null && permissionManager.hasPermission(user, Permission.VIEW, space);
                }
            },
            new PermissionCheck.LongValue<ConfluenceUser>()
            {
                @Override
                public String getParameterName()
                {
                    return CONTENT_ID;
                }

                @Override
                public boolean hasPermission(final long contentId, final ConfluenceUser user)
                {
                    ContentEntityObject content = contentEntityManager.getById(contentId);
                    return content != null && permissionManager.hasPermission(user, Permission.VIEW, content);
                }
            },
            new PermissionCheck.AlwaysAllowed<ConfluenceUser>(CONTENT_TYPE),
            new PermissionCheck.AlwaysAllowed<ConfluenceUser>(CONTENT_VERSION),
            new PermissionCheck.AlwaysAllowed<ConfluenceUser>(CONTENT_PLUGIN),
            new PermissionCheck.LongValue<ConfluenceUser>()
            {
                @Override
                public String getParameterName()
                {
                    return PAGE_ID;
                }

                @Override
                public boolean hasPermission(final long pageId, final ConfluenceUser user)
                {
                    AbstractPage page = pageManager.getAbstractPage(pageId);
                    return page != null && permissionManager.hasPermission(user, Permission.VIEW, page);
                }
            },
            new PermissionCheck.AlwaysAllowed<ConfluenceUser>(PAGE_TYPE),
            new PermissionCheck.AlwaysAllowed<ConfluenceUser>(PAGE_VERSION),
            new PermissionCheck<ConfluenceUser>()
            {
                @Override
                public String getParameterName()
                {
                    return PROFILE_KEY;
                }

                @Override
                public boolean hasPermission(final String profileKey, final ConfluenceUser currentUser)
                {
                    ConfluenceUser profileUser = userAccessor.getExistingUserByKey(new UserKey(profileKey));
                    return profileUser != null && permissionManager.hasPermission(currentUser, Permission.VIEW, profileUser);
                }
            },
            new PermissionCheck<ConfluenceUser>()
            {
                @Override
                public String getParameterName()
                {
                    return PROFILE_NAME;
                }

                @Override
                public boolean hasPermission(final String profileName, final ConfluenceUser currentUser)
                {
                    ConfluenceUser profileUser = userAccessor.getUserByName(profileName);
                    return profileUser != null && permissionManager.hasPermission(currentUser, Permission.VIEW, profileUser);
                }
            }
        );
    }

}
