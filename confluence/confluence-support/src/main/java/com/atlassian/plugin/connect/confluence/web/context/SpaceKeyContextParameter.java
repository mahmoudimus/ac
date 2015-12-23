package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@ConfluenceComponent
public class SpaceKeyContextParameter implements SpaceContextParameterMapper.SpaceParameter
{

    private static final String PARAMETER_KEY = "space.key";

    private ConfluenceContextParameterViewPermissionManager viewPermissionManager;
    private SpaceManager spaceManager;

    @Autowired
    public SpaceKeyContextParameter(ConfluenceContextParameterViewPermissionManager viewPermissionManager,
            SpaceManager spaceManager)
    {
        this.viewPermissionManager = viewPermissionManager;
        this.spaceManager = spaceManager;
    }

    @Override
    public boolean isAccessibleByCurrentUser(Space contextValue)
    {
        return viewPermissionManager.isParameterValueAccessibleByCurrentUser(contextValue);
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return Optional.ofNullable(spaceManager.getSpace(Long.valueOf(value)))
                .map(this::isAccessibleByCurrentUser).orElse(false);
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(Space contextValue)
    {
        return contextValue.getKey();
    }
}
