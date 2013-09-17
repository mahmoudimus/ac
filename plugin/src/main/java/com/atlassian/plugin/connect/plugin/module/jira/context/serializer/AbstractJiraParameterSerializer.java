package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.jira.user.CommonUserLookupJiraImpl;

public abstract class AbstractJiraParameterSerializer<T, C> extends AbstractParameterSerializer<T, C, User>
{
    public AbstractJiraParameterSerializer(UserManager userManager, String containerFieldName,
                                           ParameterUnwrapper<C, T> parameterUnwrapper,
                                           ParameterLookup<C, ?, User>... parameterLookups)
    {
        super(new CommonUserLookupJiraImpl(userManager), containerFieldName, parameterUnwrapper, parameterLookups);
    }

    public abstract static class AbstractJiraStringParameterLookup<C> extends AbstractStringParameterLookup<C, User>
    {
        public AbstractJiraStringParameterLookup(String paramName)
        {
            super(paramName);
        }
    }

    public abstract static class AbstractJiraKeyParameterLookup<C> extends AbstractJiraStringParameterLookup<C>
    {
        public AbstractJiraKeyParameterLookup()
        {
            super(KEY_FIELD_NAME);
        }
    }

    public abstract static class AbstractJiraLongParameterLookup<C> extends AbstractLongParameterLookup<C, User>
    {
        public AbstractJiraLongParameterLookup(String paramName)
        {
            super(paramName);
        }
    }

    public abstract static class AbstractJiraIdParameterLookup<C> extends AbstractJiraLongParameterLookup<C>
    {
        public AbstractJiraIdParameterLookup()
        {
            super(ID_FIELD_NAME);
        }
    }


}
