package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ReferenceContextParameterMapper implements ConnectContextParameterMapper<Plugin>
{

    private static final String CONTEXT_KEY = "plugin";
    private static final String PARAMETER_KEY = "plugin.version";

    private UserManager userManager;

    @Autowired
    public ReferenceContextParameterMapper(
            @ComponentImport UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public Optional<Plugin> extractContextValue(Map<String, Object> context)
    {
        Optional<Plugin> optionalContextValue = Optional.empty();
        Object contextValue = context.get(CONTEXT_KEY);
        if (contextValue instanceof Plugin)
        {
            optionalContextValue = Optional.of((Plugin)contextValue);
        }
        return optionalContextValue;
    }

    @Override
    public boolean isParameterValueAccessibleByCurrentUser(Plugin contextValue)
    {
        return userManager.getRemoteUser() != null;
    }

    @Override
    public String getParameterKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getParameterValue(Plugin contextValue)
    {
        return contextValue.getPluginInformation().getVersion();
    }
}
