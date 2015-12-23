package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

@ConfluenceComponent
public class ContentPluginContextParameter implements CustomContentContextParameterMapper.CustomContentParameter
{

    private static final String PARAMETER_KEY = "content.plugin";

    @Override
    public boolean isAccessibleByCurrentUser(CustomContentEntityObject contextValue)
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
    public String getValue(CustomContentEntityObject contextValue)
    {
        return contextValue.getPluginModuleKey();
    }
}
