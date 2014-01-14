package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonEventData;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class ConnectAddonEventDataBuilder extends BaseModuleBeanBuilder<ConnectAddonEventDataBuilder, ConnectAddonEventData>
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
    private String userKey;
    private String serviceEntitlementNumber;
    private String eventType;

    public ConnectAddonEventDataBuilder()
    {
        this.links = newHashMap();
    }

    public ConnectAddonEventDataBuilder(ConnectAddonEventData defaultBean)
    {
        this.links = defaultBean.getLinks();
        this.key = defaultBean.getPluginKey();
        this.clientKey = defaultBean.getClientKey();
        this.publicKey = defaultBean.getPublicKey();
        this.sharedSecret = defaultBean.getSharedSecret();
        this.serverVersion = defaultBean.getServerVersion();
        this.pluginsVersion = defaultBean.getPluginsVersion();
        this.baseUrl = defaultBean.getBaseUrl();
        this.productType = defaultBean.getProductType();
        this.description = defaultBean.getDescription();
        this.userKey = defaultBean.getUserKey();
        this.serviceEntitlementNumber = defaultBean.getServiceEntitlementNumber();
        this.eventType = defaultBean.getEventType();
    }

    public ConnectAddonEventDataBuilder withLinks(Map<String, String> params)
    {
        checkNotNull(params);

        this.links = params;
        return this;
    }

    public ConnectAddonEventDataBuilder withLink(String key, String value)
    {
        links.put(key, value);
        return this;
    }

    public ConnectAddonEventDataBuilder withPluginKey(String key)
    {
        this.key = key;
        return this;
    }

    public ConnectAddonEventDataBuilder withClientKey(String key)
    {
        this.clientKey = key;
        return this;
    }

    public ConnectAddonEventDataBuilder withPublicKey(String key)
    {
        this.publicKey = key;
        return this;
    }

    public ConnectAddonEventDataBuilder withSharedSecret(String sharedSecret)
    {
        this.sharedSecret = sharedSecret;
        return this;
    }

    public ConnectAddonEventDataBuilder withServerVersion(String version)
    {
        this.serverVersion = version;
        return this;
    }

    public ConnectAddonEventDataBuilder withPluginsVersion(String version)
    {
        this.pluginsVersion = version;
        return this;
    }

    public ConnectAddonEventDataBuilder withBaseUrl(String url)
    {
        this.baseUrl = url;
        return this;
    }

    public ConnectAddonEventDataBuilder withProductType(String type)
    {
        this.productType = type;
        return this;
    }

    public ConnectAddonEventDataBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Deprecated // Not to be used with JWT
    public ConnectAddonEventDataBuilder withUserKey(String key)
    {
        this.userKey = key;
        return this;
    }

    public ConnectAddonEventDataBuilder withServiceEntitlementNumber(String sen)
    {
        this.serviceEntitlementNumber = sen;
        return this;
    }

    public ConnectAddonEventDataBuilder withEventType(String type)
    {
        this.eventType = type;
        return this;
    }

    @Override
    public ConnectAddonEventData build()
    {
        return new ConnectAddonEventData(this);
    }
}
