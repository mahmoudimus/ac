package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceModuleContextFilter implements ModuleContextFilter
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceModuleContextFilter.class);

    public static final String PAGE_ID = "page.id";
    public static final String SPACE_ID = "space.id";
    public static final String SPACE_KEY = "space.key";

    private final PermissionManager permissionManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;
    private final SpaceManager spaceManager;
    private final PageManager pageManager;
    private final Iterable<PermissionCheck> permissionChecks;

    @Autowired
    public ConfluenceModuleContextFilter(PermissionManager permissionManager, UserAccessor userAccessor,
            UserManager userManager, SpaceManager spaceManager, PageManager pageManager)
    {
        this.permissionManager = permissionManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
        permissionChecks = constructPermissionChecks();
    }

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters();

        UserKey userKey = userManager.getRemoteUserKey();
        ConfluenceUser currentUser = userAccessor.getExistingUserByKey(userKey);

        for (PermissionCheck permissionCheck : permissionChecks)
        {
            String value = unfiltered.get(permissionCheck.getParameterName());
            if (!Strings.isNullOrEmpty(value) && permissionCheck.hasPermission(value, currentUser))
            {
                filtered.put(permissionCheck.getParameterName(), value);
            }
        }
        return filtered;
    }

    private static interface PermissionCheck
    {
        String getParameterName();

        boolean hasPermission(String value, ConfluenceUser user);
    }

    private static abstract class LongValuePermissionCheck implements PermissionCheck
    {
        @Override
        public boolean hasPermission(final String value, final ConfluenceUser user)
        {
            long longValue;
            try
            {
                longValue = Long.parseLong(value);
            }
            catch (NumberFormatException e)
            {
                log.debug("Failed to parse " + getParameterName(), e);
                return false;
            }
            return hasPermission(longValue, user);
        }

        abstract boolean hasPermission(long value, ConfluenceUser user);
    }

    private Iterable<PermissionCheck> constructPermissionChecks()
    {
        return ImmutableList.of(
                new PermissionCheck()
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
                new LongValuePermissionCheck()
                {
                    @Override
                    public String getParameterName()
                    {
                        return SPACE_ID;
                    }

                    @Override
                    boolean hasPermission(final long spaceId, final ConfluenceUser user)
                    {
                        Space space = spaceManager.getSpace(spaceId);
                        return space != null && permissionManager.hasPermission(user, Permission.VIEW, space);
                    }
                },
                new LongValuePermissionCheck()
                {
                    @Override
                    public String getParameterName()
                    {
                        return PAGE_ID;
                    }

                    @Override
                    boolean hasPermission(final long pageId, final ConfluenceUser user)
                    {
                        Page page = pageManager.getPage(pageId);
                        return page != null && permissionManager.hasPermission(user, Permission.VIEW, page);
                    }
                }
        );
    }

}
