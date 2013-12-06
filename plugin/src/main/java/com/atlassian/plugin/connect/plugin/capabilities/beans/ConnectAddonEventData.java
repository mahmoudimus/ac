package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonEventDataBuilder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Represents the data that an addon will receive with lifecycle events.
 */
public class ConnectAddonEventData extends BaseCapabilityBean
{
    private Map<String,String> links;
    //this is the plugin key
    private String key;
    private String clientKey;
    private String publicKey;
    private String sharedSecret; // optional
    private String serverVersion;
    private String pluginsVersion;
    private String baseUrl;
    private String productType;
    private String description;
    private String userKey;
    private String serviceEntitlementNumber;
    private String eventType;
    
    
    public ConnectAddonEventData()
    {
        this.key = "";
        this.links = newHashMap();
        this.clientKey = "";
        this.publicKey = "";
        this.sharedSecret = "";
        this.serverVersion = "";
        this.pluginsVersion = "";
        this.baseUrl = "";
        this.productType = "";
        this.description = "";
        this.userKey = "";
        this.serviceEntitlementNumber = "";
        this.eventType = "";
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
        if (null == sharedSecret)
        {
            this.sharedSecret = "";
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
        if(null == description)
        {
            this.description = "";
        }
        if(null == userKey)
        {
            this.userKey = "";
        }
        if(null == serviceEntitlementNumber)
        {
            this.serviceEntitlementNumber = "";
        }
        if(null == eventType)
        {
            this.eventType = "";
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

    public String getSharedSecret()
    {
        return sharedSecret;
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

    public String getDescription()
    {
        return description;
    }

    public String getUserKey()
    {
        return userKey;
    }

    public String getServiceEntitlementNumber()
    {
        return serviceEntitlementNumber;
    }

    public String getEventType()
    {
        return eventType;
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
