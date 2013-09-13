package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.SpaceSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.user.EntityException;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.atlassian.confluence.security.SpacePermission.VIEWSPACE_PERMISSION;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts space parameters that can be included in webpanel's iframe url.
 */
public class SpaceContextMapParameterExtractor implements ContextMapParameterExtractor<Space>
{
    private static final String SPACE_CONTEXT_PARAMETER = "space";
    private SpaceSerializer spaceSerializer;
    private final UserManager userManager;
    private final SpacePermissionManager permissionManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceContextMapParameterExtractor.class);


    public SpaceContextMapParameterExtractor(SpaceSerializer spaceSerializer, SpacePermissionManager permissionManager, UserManager userManager)
    {
        this.spaceSerializer = spaceSerializer;
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }

    @Override
    public Optional<Space> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getSpace())
            {
                return Optional.of(webInterfaceContext.getSpace());
            }
        } else if (context.containsKey(SPACE_CONTEXT_PARAMETER) && context.get(SPACE_CONTEXT_PARAMETER) instanceof Space)
        {
            return Optional.of((Space) context.get(SPACE_CONTEXT_PARAMETER));
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<Space> serializer()
    {
        return spaceSerializer;
    }

    @Override
    public boolean hasViewPermission(String username, Space space)
    {
        try
        {
            return permissionManager.hasPermission(VIEWSPACE_PERMISSION, space, userManager.getUser(username));
        }
        catch (EntityException e)
        {
            LOGGER.error("Failed to check permission. Defaulting to denying access", e);
            return false;
        }
    }
}
