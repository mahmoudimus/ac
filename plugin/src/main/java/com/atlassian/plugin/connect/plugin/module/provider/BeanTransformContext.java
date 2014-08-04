package com.atlassian.plugin.connect.plugin.module.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.util.ProductFilter;

public class BeanTransformContext
{
    private final Plugin theConnectPlugin;
    private final ProductFilter appFilter;

    public BeanTransformContext(Plugin theConnectPlugin, ProductFilter appFilter)
    {
        this.theConnectPlugin = theConnectPlugin;
        this.appFilter = appFilter;
    }

    public Plugin getTheConnectPlugin()
    {
        return theConnectPlugin;
    }

    public ProductFilter getAppFilter()
    {
        return appFilter;
    }
}
