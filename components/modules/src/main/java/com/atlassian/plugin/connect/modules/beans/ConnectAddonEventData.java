package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonEventDataBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Represents the data that an addon will receive with lifecycle events.
 */
public class ConnectAddonEventData extends BaseModuleBean
{
    private Map<String, String> links;
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
    @Deprecated
    private String userKey; // Not to be used with JWT (should be in the token)
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

        if (null == key)
        {
            this.key = "";
        }

        if (null == links)
        {
            this.links = newHashMap();
        }
        if (null == clientKey)
        {
            this.clientKey = "";
        }
        if (null == publicKey)
        {
            this.publicKey = "";
        }
        if (null == sharedSecret)
        {
            this.sharedSecret = "";
        }
        if (null == serverVersion)
        {
            this.serverVersion = "";
        }
        if (null == pluginsVersion)
        {
            this.pluginsVersion = "";
        }
        if (null == baseUrl)
        {
            this.baseUrl = "";
        }
        if (null == productType)
        {
            this.productType = "";
        }
        if (null == description)
        {
            this.description = "";
        }
        if (null == userKey)
        {
            this.userKey = "";
        }
        if (null == serviceEntitlementNumber)
        {
            this.serviceEntitlementNumber = "";
        }
        if (null == eventType)
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

    @Deprecated // Not to be used with JWT
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

    // don't call super because BaseModuleBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ConnectAddonEventData))
        {
            return false;
        }

        ConnectAddonEventData other = (ConnectAddonEventData) otherObj;

        return new EqualsBuilder()
                .append(key, other.key)
                .append(links, other.links)
                .append(clientKey, other.clientKey)
                .append(publicKey, other.publicKey)
                .append(sharedSecret, other.sharedSecret)
                .append(serverVersion, other.serverVersion)
                .append(pluginsVersion, other.pluginsVersion)
                .append(baseUrl, other.baseUrl)
                .append(productType, other.productType)
                .append(description, other.description)
                .append(userKey, other.userKey)
                .append(serviceEntitlementNumber, other.serviceEntitlementNumber)
                .append(eventType, other.eventType)
                .isEquals();
    }

    // don't call super because BaseAddonBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(41, 7)
                .append(key)
                .append(links)
                .append(clientKey)
                .append(publicKey)
                .append(sharedSecret)
                .append(serverVersion)
                .append(pluginsVersion)
                .append(baseUrl)
                .append(productType)
                .append(description)
                .append(userKey)
                .append(serviceEntitlementNumber)
                .append(eventType)
                .build();
    }
}
