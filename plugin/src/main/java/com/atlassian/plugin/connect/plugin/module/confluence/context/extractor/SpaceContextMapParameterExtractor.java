package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.SpaceSerializer;
import com.atlassian.user.UserManager;

/**
 * Extracts space parameters that can be included in webpanel's iframe url.
 */
public class SpaceContextMapParameterExtractor extends AbstractConfluenceContextMapParameterExtractor<Space>
{
    private static final String SPACE_CONTEXT_PARAMETER = "space";

    public SpaceContextMapParameterExtractor(SpaceSerializer spaceSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        super(Space.class, spaceSerializer, SPACE_CONTEXT_PARAMETER, permissionManager, userManager);
    }

    @Override
    protected Space getResource(WebInterfaceContext webInterfaceContext)
    {
        return webInterfaceContext.getSpace();
    }
}