package com.atlassian.plugin.connect.plugin.capabilities.testobjects.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;

@CapabilitySet(key = "beer-modules", moduleProvider = ConnectModuleProvider.class)
public class BeerModule implements CapabilityBean
{
    private String key;
    private String brand;

    public String getKey()
    {
        return key;
    }

    public String getBrand()
    {
        return brand;
    }
}
