package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.api.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.api.capabilities.beans.CapabilityBean;

@CapabilitySet(key = "some-modules")
public class SomeModule implements CapabilityBean
{
    private String key;
    private String food;

    public String getKey()
    {
        return key;
    }

    @Override
    public I18nProperty getName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public I18nProperty getDescription()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getFood()
    {
        return food;
    }
}
