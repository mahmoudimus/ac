package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.AbstractParameterSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.user.CommonUserLookupJiraImpl;

public abstract class AbstractJiraParameterSerializer<R, W> extends AbstractParameterSerializer<R, W, User>
{
    public AbstractJiraParameterSerializer(UserManager userManager, String containerFieldName,
                                           ParameterUnwrapper<W, R> parameterUnwrapper,
                                           ParameterLookup<W, ?, User>... parameterLookups)
    {
        super(new CommonUserLookupJiraImpl(userManager), containerFieldName, parameterUnwrapper, parameterLookups);
    }

    public abstract static class AbstractJiraStringParameterLookup<W> extends AbstractStringParameterLookup<W, User>
    {
        public AbstractJiraStringParameterLookup(String paramName)
        {
            super(paramName);
        }
    }

    public abstract static class AbstractJiraKeyParameterLookup<W> extends AbstractJiraStringParameterLookup<W>
    {
        public AbstractJiraKeyParameterLookup()
        {
            super(KEY_FIELD_NAME);
        }
    }

    public abstract static class AbstractJiraLongParameterLookup<W> extends AbstractLongParameterLookup<W, User>
    {
        public AbstractJiraLongParameterLookup(String paramName)
        {
            super(paramName);
        }
    }

    public abstract static class AbstractJiraIdParameterLookup<W> extends AbstractJiraLongParameterLookup<W>
    {
        public AbstractJiraIdParameterLookup()
        {
            super(ID_FIELD_NAME);
        }
    }

    @Override
    protected boolean isResultValid(W serviceResult)
    {
        return !(serviceResult instanceof ServiceResult) || ((ServiceResult)serviceResult).isValid();

    }
}
