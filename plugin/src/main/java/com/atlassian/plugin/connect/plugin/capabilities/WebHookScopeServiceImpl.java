package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
import org.springframework.stereotype.Component;

@Component
public class WebHookScopeServiceImpl implements WebHookScopeService
{
    @Override
    public ScopeName getRequiredScope(String webHookKey)
    {
        return ScopeName.READ; // currently, all of our web hooks require the READ scope
    }
}
