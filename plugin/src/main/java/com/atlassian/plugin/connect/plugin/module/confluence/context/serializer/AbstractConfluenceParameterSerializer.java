package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.confluence.user.CommonUserLookupConfluenceImpl;
import com.atlassian.plugin.connect.plugin.module.context.AbstractParameterSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

import static com.atlassian.confluence.security.Permission.VIEW;

public abstract class AbstractConfluenceParameterSerializer<T, C> extends AbstractParameterSerializer<T, C, User>
{
    private final PermissionManager permissionManager;

    public AbstractConfluenceParameterSerializer(UserManager userManager, PermissionManager permissionManager,
                                                 String containerFieldName,
                                                 ParameterUnwrapper<C, T> parameterUnwrapper,
                                                 ParameterLookup<C, ?, User>... parameterLookups)
    {
        super(new CommonUserLookupConfluenceImpl(userManager), containerFieldName, parameterUnwrapper, parameterLookups);
        this.permissionManager = permissionManager;
    }

    public abstract static class AbstractConfluenceStringParameterLookup<C> extends AbstractStringParameterLookup<C, User>
    {
        public AbstractConfluenceStringParameterLookup(String paramName)
        {
            super(paramName);
        }
    }

    public abstract static class AbstractConfluenceKeyParameterLookup<C> extends AbstractConfluenceStringParameterLookup<C>
    {
        public AbstractConfluenceKeyParameterLookup()
        {
            super(KEY_FIELD_NAME);
        }
    }

    public abstract static class AbstractConfluenceLongParameterLookup<C> extends AbstractLongParameterLookup<C, User>
    {
        public AbstractConfluenceLongParameterLookup(String paramName)
        {
            super(paramName);
        }
    }

    public abstract static class AbstractConfluenceIdParameterLookup<C> extends AbstractConfluenceLongParameterLookup<C>
    {
        public AbstractConfluenceIdParameterLookup()
        {
            super(ID_FIELD_NAME);
        }
    }

    @Override
    protected void checkViewPermission(T resource, User user) throws UnauthorisedException, ResourceNotFoundException
    {
        if (!permissionManager.hasPermission(user, VIEW, resource))
        {
            // TODO: Assuming confluence has same policy as Jira and return's 404's rather than 401's. Check
            throwResourceNotFoundException();
        }
    }


}
