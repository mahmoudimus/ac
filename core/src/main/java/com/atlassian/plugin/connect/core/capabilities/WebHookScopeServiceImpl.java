package com.atlassian.plugin.connect.core.capabilities;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

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
