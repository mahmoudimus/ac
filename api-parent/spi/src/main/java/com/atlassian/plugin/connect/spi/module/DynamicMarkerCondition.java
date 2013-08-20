package com.atlassian.plugin.connect.spi.module;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

/**
 * Marker condition to support dynamic conditions
 */
public final class DynamicMarkerCondition implements Condition
{
    public DynamicMarkerCondition()
    {
        int x = 0;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        // no op
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return true;
    }
}
