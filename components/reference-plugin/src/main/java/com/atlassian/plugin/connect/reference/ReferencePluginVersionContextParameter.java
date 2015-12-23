package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReferencePluginVersionContextParameter implements ReferenceContextParameterMapper.PluginParameter
{

    private static final String PARAMETER_KEY = "plugin.version";

    private UserManager userManager;

    @Autowired
    public ReferencePluginVersionContextParameter(@ComponentImport UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public boolean isAccessibleByCurrentUser(Plugin contextValue)
    {
        return isValueAccessibleByCurrentUser(getValue(contextValue));
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return userManager.getRemoteUser() != null;
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(Plugin contextValue)
    {
        return contextValue.getPluginInformation().getVersion();
    }
}
