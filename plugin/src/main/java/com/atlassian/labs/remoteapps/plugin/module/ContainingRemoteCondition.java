package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Marker condition to indicate a remote condition somewhere in this condition set
 */
public class ContainingRemoteCondition implements Condition
{
    private final Condition delegate;
    private final String conditionUrl;

    public ContainingRemoteCondition(Condition delegate, String conditionUrl)
    {
        this.delegate = delegate;
        this.conditionUrl = conditionUrl;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        delegate.init(params);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return delegate.shouldDisplay(context);
    }

    public String getConditionUrl()
    {
        return conditionUrl;
    }
}
