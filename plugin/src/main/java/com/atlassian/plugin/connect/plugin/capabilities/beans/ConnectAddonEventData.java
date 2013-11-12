package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonEventDataBuilder;

import static com.google.common.collect.Maps.newHashMap;

public class ConnectAddonEventData extends BaseCapabilityBean
{
    private Map<String,String> links;
    //this is the plugin key
    private String key;
    private String clientKey;
    private String publicKey;
    private String serverVersion;
    private String pluginsVersion;
    private String baseUrl;
    private String productType;
    private String decription;
    private String user_key;
    
    
    public ConnectAddonEventData()
    {
        this.key = "";
        this.links = newHashMap();
        this.clientKey = "";
        this.publicKey = "";
        this.serverVersion = "";
        this.pluginsVersion = "";
        this.baseUrl = "";
        this.productType = "";
        this.decription = "";
        this.user_key = "";
    }

    public ConnectAddonEventData(ConnectAddonEventDataBuilder builder)
    {
        super(builder);

        if(null == key)
        {
            this.key = "";
        }
        
        if(null == links)
        {
            this.links = newHashMap();
        }
        if(null == clientKey)
        {
            this.clientKey = "";
        }
        if(null == publicKey)
        {
            this.publicKey = "";
        }
        if(null == serverVersion)
        {
            this.serverVersion = "";
        }
        if(null == pluginsVersion)
        {
            this.pluginsVersion = "";
        }
        if(null == baseUrl)
        {
            this.baseUrl = "";
        }
        if(null == productType)
        {
            this.productType = "";
        }
        if(null == decription)
        {
            this.decription = "";
        }
        if(null == user_key)
        {
            this.user_key = "";
        }
    }

    public Map<String, String> getLinks()
    {
        return links;
    }
    
    public String getPluginKey()
    {
        return key;
    }
    
    public String getClientKey()
    {
        return clientKey;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public String getServerVersion()
    {
        return serverVersion;
    }

    public String getPluginsVersion()
    {
        return pluginsVersion;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getProductType()
    {
        return productType;
    }

    public String getDecription()
    {
        return decription;
    }

    public String getUserKey()
    {
        return user_key;
    }

    public static ConnectAddonEventDataBuilder newConnectAddonEventData()
    {
        return new ConnectAddonEventDataBuilder();
    }

    public static ConnectAddonEventDataBuilder newConnectAddonEventData(ConnectAddonEventData defaultBean)
    {
        return new ConnectAddonEventDataBuilder(defaultBean);
    }
}
