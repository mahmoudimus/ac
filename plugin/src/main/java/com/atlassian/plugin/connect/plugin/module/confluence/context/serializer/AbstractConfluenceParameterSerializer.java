package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.plugin.connect.plugin.module.confluence.user.CommonUserLookupConfluenceImpl;
import com.atlassian.plugin.connect.plugin.module.context.AbstractParameterSerializer;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

public abstract class AbstractConfluenceParameterSerializer<T, C> extends AbstractParameterSerializer<T, C, User>
{
    public AbstractConfluenceParameterSerializer(UserManager userManager, String containerFieldName,
                                                 ParameterUnwrapper<C, T> parameterUnwrapper,
                                                 ParameterLookup<C, ?, User>... parameterLookups)
    {
        super(new CommonUserLookupConfluenceImpl(userManager), containerFieldName, parameterUnwrapper, parameterLookups);
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


}
