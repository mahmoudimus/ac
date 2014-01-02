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
 * The add-on descriptor is a JSON file (`atlassian-connect.json`) that describes the add-on to the Atlassian application.
 * The descriptor includes general information for the add-on, as well as the modules that the add-on wants to use or
 * extend.
 *
 * If you're familiar with Java add-on development with previous versions of the Atlassian Plugin Framework, you may already be
 * familiar with the `atlassian-plugin.xml` descriptors. The `atlassian-connect.json` serves the same function.
 *
 * The descriptor serves as the glue between the remote add-on and the Atlassian application. When an administrator for an
 * Atlassian OnDemand instance installs an add-on, what they are really doing is installing this descriptor file, which
 * contains pointers to your service. You can see an example below.
 *
 * For details and application-specific reference information on the descriptor please refer to the "jira modules"
 * and "confluence modules" sections of this documentation. But we'll call out a few highlights from the example here.
 *
 * The version element identifies the version of the add-on itself. Note that versioning works differently for Atlassian
 * Connect add-ons than it does for traditional, in-process add-ons.
 *
 * Since Atlassian Connect add-ons are remote and largely independent from the Atlassian application, they can be changed
 * at any time, without having to create a new version or report the change to the Atlassian instance. The changes are
 * reflected in the Atlassian instance immediately (or at least at page reload time).
 *
 * However, some add-on changes do require a change in the descriptor file itself. For example, say you modify the add-on
 * to have a new page module. Since this requires a page module declaration in the descriptor, it means making an updated
 * descriptor available, which instances will have to re-register. To propagate this change, you need to create a new version
 * of the add-on in its Marketplace listing. The Marketplace will take care of the rest: informing administrators
 * and automatically installing the available update.
 *
 *
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ADDON_EXAMPLE}
 * @exampleJson <p class="expandNextPre">Full example with all modules:</p> {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#ADDON_COMPLETE_EXAMPLE}
 * @schemaTitle Addon Descriptor
 * @since 1.0
 */
public class ConnectAddonBean extends BaseModuleBean
{
    public static final int DEFAULT_WEIGHT = 100;

    /**
     * A unique key to identify the add-on
     */
    @Required
    private String key;

    /**
     * The human-readable name of the add-on
     */
    private String name;

    /**
     * The version of the add-on
     */
    private String version;

    /**
     * A human readable description of what the add-on does. The description will be visible in the `Manage Add-ons`
     * section of the administration console. Provide meaningful and identifying information for the instance administrator.
     */
    private String description;

    /**
     * The vendor who is offering the add-on
     */
    private VendorBean vendor;

    /**
     * A set of links that the add-on wishes to publish
     * @exampleJson {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#LINKS_EXAMPLE}
     */
    private Map<String, String> links;

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
