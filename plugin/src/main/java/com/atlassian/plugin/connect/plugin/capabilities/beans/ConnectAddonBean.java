package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.LifecycleBean.newLifecycleBean;
import static com.google.common.collect.Maps.newHashMap;

/**
 * The root descriptor for an Atlassian Connect add on
 *
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ADDON_EXAMPLE}
 * @exampleJson Kitchen Sink: <p class="expandNextPre"></p>{@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ADDON_COMPLETE_EXAMPLE}
 * @schemaTitle Connect Addon Root Descriptor
 * @since 1.0
 */
public class ConnectAddonBean extends BaseModuleBean
{
    public static final int DEFAULT_WEIGHT = 100;

    /**
     * A unique key to identify the add on
     */
    @Required
    private String key;

    /**
     * The human-readable name of the add on
     */
    private String name;
    
    private String version;

    /**
     * A human readable description of what the add on does
     */
    private String description;

    /**
     * The vendor who is offering the add on
     */
    private VendorBean vendor;
    private Map<String,String> links;

    /**
     * Allows the add on to register for plugin lifecycle notifications
     */
    private LifecycleBean lifecycle;

    /**
     * The base url of the remote add on
     */
    @Required
    @StringSchemaAttributes(format = "uri")
    private String baseUrl;

    /**
     * Defines the authentication type to use when signing requests between the host application and the connect add on.
     */
    private AuthenticationBean authentication;

    /**
     * Whether or not to enable licensing options in the UPM.Marketplace for this add on
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean enableLicensing;

    /**
     * The list of modules this add on provides
     */
    private ModuleList modules;
    
    public ConnectAddonBean()
    {
        this.key = "";
        this.name = "";
        this.version = "1.0";
        this.description = "";
        this.vendor = VendorBean.newVendorBean().build();
        this.links = newHashMap();
        this.lifecycle = newLifecycleBean().build();
        this.modules = new ModuleList();
        this.baseUrl = "";
        this.authentication = newAuthenticationBean().build();
        this.enableLicensing = null;
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

        if(null == modules)
        {
            this.modules = new ModuleList();
        }

        if(null == vendor)
        {
            this.vendor = VendorBean.newVendorBean().build();
        }
        
        if(null == links)
        {
            this.links = newHashMap();
        }
        
        if(null == lifecycle)
        {
            this.lifecycle = newLifecycleBean().build();
        }
        if(null == baseUrl)
        {
            this.baseUrl = "";
        }
        if(null == authentication)
        {
            this.authentication = newAuthenticationBean().build();
        }
    }

    public String getKey()
    {
        return key;
    }

    /**
     * the name of the addon
     * @return
     */
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

    public ModuleList getModules()
    {
        return modules;
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    public LifecycleBean getLifecycle()
    {
        return lifecycle;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public AuthenticationBean getAuthentication()
    {
        return authentication;
    }

    public Boolean getEnableLicensing()
    {
        return enableLicensing;
    }

    public static ConnectAddonBeanBuilder newConnectAddonBean()
    {
        return new ConnectAddonBeanBuilder();
    }

    public static ConnectAddonBeanBuilder newConnectAddonBean(ConnectAddonBean defaultBean)
    {
        return new ConnectAddonBeanBuilder(defaultBean);
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ConnectAddonBean))
        {
            return false;
        }

        ConnectAddonBean other = (ConnectAddonBean) otherObj;

        return new EqualsBuilder()
                .append(key, other.key)
                .append(name, other.name)
                .append(version, other.version)
                .append(description, other.description)
                .append(vendor, other.vendor)
                .append(links, other.links)
                .append(lifecycle, other.lifecycle)
                .append(baseUrl, other.baseUrl)
                .append(authentication, other.authentication)
                .append(enableLicensing, other.enableLicensing)
                .append(modules, other.modules)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(41, 7)
                .append(key)
                .append(name)
                .append(version)
                .append(description)
                .append(vendor)
                .append(links)
                .append(lifecycle)
                .append(baseUrl)
                .append(authentication)
                .append(enableLicensing)
                .append(modules)
                .build();
    }
}
