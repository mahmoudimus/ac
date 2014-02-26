package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.pages.AbstractPage;
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
    public static final String PAGE_VERSION = "page.version";
    public static final String PAGE_TYPE = "page.type";
    public static final String SPACE_ID = "space.id";
    public static final String SPACE_KEY = "space.key";

    private final PermissionManager permissionManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;
    private final SpaceManager spaceManager;
    private final PageManager pageManager;

    @Autowired
    public ConfluenceModuleContextFilter(PermissionManager permissionManager, UserAccessor userAccessor,
            UserManager userManager, SpaceManager spaceManager, PageManager pageManager)
    {
        this.permissionManager = permissionManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
    }

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters();

        UserKey userKey = userManager.getRemoteUserKey();
        ConfluenceUser currentUser = userAccessor.getExistingUserByKey(userKey);

        String spaceKey = unfiltered.get(SPACE_KEY);
        String spaceIdValue = unfiltered.get(SPACE_ID);
        Long spaceId = parseLong(spaceIdValue, SPACE_ID);

        Space space;
        boolean checkSpaceIdPermission = true;

        if (spaceKey != null)
        {
            space = spaceManager.getSpace(spaceKey);
            if (space != null && permissionManager.hasPermission(currentUser, Permission.VIEW, space))
            {
                filtered.put(SPACE_KEY, spaceKey);
                if (spaceId != null && space.getId() == spaceId)
                {
                    filtered.put(SPACE_ID, spaceIdValue);
                    checkSpaceIdPermission = false;
                }
            }
        }

        if (spaceId != null && checkSpaceIdPermission)
        {
            space = spaceManager.getSpace(spaceId);
            if (space != null && permissionManager.hasPermission(currentUser, Permission.VIEW, space))
            {
                filtered.put(SPACE_ID, spaceIdValue);
            }
        }

        String pageIdValue = unfiltered.get(PAGE_ID);
        Long pageId = parseLong(pageIdValue, PAGE_ID);
        if (pageId != null)
        {
            AbstractPage page = pageManager.getAbstractPage(pageId);
            if (page != null && permissionManager.hasPermission(currentUser, Permission.VIEW, page))
            {
                filtered.put(PAGE_ID, pageIdValue);
                filtered.put(PAGE_VERSION, unfiltered.get(PAGE_VERSION));
                filtered.put(PAGE_TYPE, unfiltered.get(PAGE_TYPE));
            }
        }

        return filtered;
    }


    Long parseLong(String value, String field)
    {
        try
        {
            return value == null ? null : Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            log.debug("Failed to parse " + field + " " + value + " as a number", e);
        }
        return null;
    }
}
