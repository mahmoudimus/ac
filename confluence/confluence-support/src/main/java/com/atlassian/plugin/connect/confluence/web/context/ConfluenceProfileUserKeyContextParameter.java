package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@ConfluenceComponent
public class ConfluenceProfileUserKeyContextParameter implements ConfluenceProfileUserContextParameterMapper.UserParameter
{

    private static final String PARAMETER_KEY = "profileUser.key";

    private ConfluenceContextParameterViewPermissionManager viewPermissionManager;
    private UserAccessor userAccessor;

    @Autowired
    public ConfluenceProfileUserKeyContextParameter(ConfluenceContextParameterViewPermissionManager viewPermissionManager,
            UserAccessor userAccessor)
    {
        this.viewPermissionManager = viewPermissionManager;
        this.userAccessor = userAccessor;
    }

    @Override
    public boolean isAccessibleByCurrentUser(ConfluenceUser contextValue)
    {
        return viewPermissionManager.isParameterValueAccessibleByCurrentUser(contextValue);
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return Optional.ofNullable(userAccessor.getUserByName(value))
                .map(this::isAccessibleByCurrentUser).orElse(false);
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(ConfluenceUser contextValue)
    {
        return contextValue.getKey().getStringValue();
    }
}
