package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.SpaceService;
import com.atlassian.confluence.content.service.space.SpaceLocator;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes space objects.
 */
public class SpaceSerializer extends AbstractConfluenceParameterSerializer<Space, SpaceLocator>
{
   public static final String SPACE_FIELD_NAME = "space";

    public SpaceSerializer(final SpaceService spaceService, UserManager userManager)
    {
        super(userManager, SPACE_FIELD_NAME,
                new ParameterUnwrapper<SpaceLocator, Space>()
                {
                    @Override
                    public Space unwrap(SpaceLocator wrapped)
                    {
                        return wrapped.getSpace();
                    }
                },
                // There is no lookup by id
//                new AbstractConfluenceIdParameterLookup<SpaceLocator>()
//                {
//                    @Override
//                    public SpaceLocator lookup(User user, Long id)
//                    {
//                        // TODO: The confluence space service does not check permissions. Need to do that somewhere
//                        return spaceService.getIdSpaceLocator(id);
//                    }
//                },
                new AbstractConfluenceKeyParameterLookup<SpaceLocator>()
                {
                    @Override
                    public SpaceLocator lookup(User user, String key)
                    {
                        // TODO: The confluence space service does not check permissions. Need to do that somewhere
                        return spaceService.getKeySpaceLocator(key);
                    }
                }
        );
    }

    @Override
    public Map<String, Object> serialize(final Space space)
    {
        return ImmutableMap.<String, Object>of(SPACE_FIELD_NAME,
                ImmutableMap.of(ID_FIELD_NAME, space.getId(),
                        KEY_FIELD_NAME, space.getKey()));
    }

    @Override
    protected boolean isResultValid(SpaceLocator serviceResult)
    {
        return serviceResult.getSpace() != null;
    }
}
