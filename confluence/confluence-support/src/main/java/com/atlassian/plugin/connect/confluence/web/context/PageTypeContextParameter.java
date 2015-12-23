package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

@ConfluenceComponent
public class PageTypeContextParameter implements PageContextParameterMapper.PageParameter
{

    private static final String PARAMETER_KEY = "page.type";

    @Override
    public boolean isAccessibleByCurrentUser(AbstractPage contextValue)
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
    public String getValue(AbstractPage contextValue)
    {
        return contextValue.getType();
    }
}
