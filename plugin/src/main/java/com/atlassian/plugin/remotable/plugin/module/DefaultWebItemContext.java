package com.atlassian.plugin.remotable.plugin.module;

import java.util.Map;

public class DefaultWebItemContext implements WebItemContext
{
    private final Map<String,String> contextParams;
    private final int preferredWeight;
    private final String preferredSectionKey;

    public DefaultWebItemContext(String preferredSectionKey, int preferredWeight, Map<String, String> contextParams)
    {
        this.preferredSectionKey = preferredSectionKey;
        this.preferredWeight = preferredWeight;
        this.contextParams = contextParams;
    }

    @Override
    public Map<String, String> getContextParams()
    {
        return contextParams;
    }

    @Override
    public int getPreferredWeight()
    {
        return preferredWeight;
    }

    @Override
    public String getPreferredSectionKey()
    {
        return preferredSectionKey;
    }
}
