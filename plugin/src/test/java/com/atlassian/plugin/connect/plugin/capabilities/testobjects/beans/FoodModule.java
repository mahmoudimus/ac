package com.atlassian.plugin.connect.plugin.capabilities.testobjects.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;

@CapabilitySet(key = "food-modules", moduleProvider = ConnectModuleProvider.class)
public class FoodModule implements CapabilityBean
{
    private String key;
    private String food;

    public String getKey()
    {
        return key;
    }

    public String getFood()
    {
        return food;
    }
}
