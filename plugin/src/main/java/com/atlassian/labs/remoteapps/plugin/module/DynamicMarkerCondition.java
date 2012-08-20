package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Marker condition to support dynamic conditions
 */
public class DynamicMarkerCondition implements Condition
{
    public DynamicMarkerCondition()
    {
        int x = 0;

    }
    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        throw new UnsupportedOperationException("Only a marker");
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        throw new UnsupportedOperationException("Only a marker");
    }
}
