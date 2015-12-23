package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

@ConfluenceComponent
public class ContentVersionContextParameter implements ContentContextParameterMapper.ContentParameter
{

    private static final String PARAMETER_KEY = "content.version";

    @Override
    public boolean isAccessibleByCurrentUser(ContentEntityObject contextValue)
    {
        return true;
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return true;
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(ContentEntityObject contextValue)
    {
        return Long.toString(contextValue.getVersion());
    }
}
