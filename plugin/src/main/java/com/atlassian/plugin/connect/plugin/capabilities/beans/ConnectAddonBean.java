package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @since 1.0
 */
public class ConnectAddonBean extends BaseCapabilityBean
{
    private String key;
    private String name;
    private String version;
    private String description;
    private VendorBean vendor;
    private Map<String,String> links;
    private Map<String,List<? extends CapabilityBean>> capabilities;
    
    public ConnectAddonBean()
    {
        this.key = "";
        this.name = "";
        this.version = "1.0";
        this.description = "";
        this.vendor = VendorBean.newVendorBean().build();
        this.links = newHashMap();
        this.capabilities = newHashMap();
    }

    public ConnectAddonBean(ConnectAddonBeanBuilder builder)
    {
        super(builder);

        if(null == key)
        {
            this.key = "";
        }
        
        if(null == name)
        {
            this.name = "";
        }
        
        if(null == version)
        {
            this.version = "1.0";
        }

        if(null == description)
        {
            this.description = "";
        }

        if(null == capabilities)
        {
            this.capabilities = newHashMap();
        }

        if(null == vendor)
        {
            this.vendor = VendorBean.newVendorBean().build();
        }
        
        if(null == links)
        {
            this.links = newHashMap();
        }
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public String getDescription()
    {
        return description;
    }

    public VendorBean getVendor()
    {
        return vendor;
    }

    public Map<String,List<? extends CapabilityBean>> getCapabilities()
    {
        return capabilities;
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    public static ConnectAddonBeanBuilder newConnectAddonBean()
    {
        return new ConnectAddonBeanBuilder();
    }

    public static ConnectAddonBeanBuilder newConnectAddonBean(ConnectAddonBean defaultBean)
    {
        return new ConnectAddonBeanBuilder(defaultBean);
    }
}
