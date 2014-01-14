package com.atlassian.plugin.connect.modules.beans;

import java.util.Map;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.google.common.base.Function;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.google.common.collect.Maps.newHashMap;

/**
 * The add-on descriptor is a JSON file (`atlassian-connect.json`) that describes the add-on to the Atlassian application.
 * The descriptor includes general information for the add-on, as well as the modules that the add-on wants to use or
 * extend.
 * <p/>
 * If you're familiar with Java add-on development with previous versions of the Atlassian Plugin Framework, you may already be
 * familiar with the `atlassian-plugin.xml` descriptors. The `atlassian-connect.json` serves the same function.
 * <p/>
 * The descriptor serves as the glue between the remote add-on and the Atlassian application. When an administrator for an
 * Atlassian OnDemand instance installs an add-on, what they are really doing is installing this descriptor file, which
 * contains pointers to your service. You can see an example below.
 * <p/>
 * For details and application-specific reference information on the descriptor please refer to the "jira modules"
 * and "confluence modules" sections of this documentation. But we'll call out a few highlights from the example here.
 * <p/>
 * The version element identifies the version of the add-on itself. Note that versioning works differently for Atlassian
 * Connect add-ons than it does for traditional, in-process add-ons.
 * <p/>
 * Since Atlassian Connect add-ons are remote and largely independent from the Atlassian application, they can be changed
 * at any time, without having to create a new version or report the change to the Atlassian instance. The changes are
 * reflected in the Atlassian instance immediately (or at least at page reload time).
 * <p/>
 * However, some add-on changes do require a change in the descriptor file itself. For example, say you modify the add-on
 * to have a new page module. Since this requires a page module declaration in the descriptor, it means making an updated
 * descriptor available, which instances will have to re-register. To propagate this change, you need to create a new version
 * of the add-on in its Marketplace listing. The Marketplace will take care of the rest: informing administrators
 * and automatically installing the available update. See [Upgrades](../concepts/upgrades.html) for more details.
 * <p/>
 * <p/>
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ADDON_EXAMPLE}
 * @exampleJson Kitchen Sink: <p class="expandNextPre"></p>{@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ADDON_COMPLETE_EXAMPLE}
 * @schemaTitle Addon Descriptor
 * @since 1.0
 */
public class ConnectAddonBean extends BaseModuleBean
{
    public static final String KEY_ATTR = "key";
    public static final String BASE_URL_ATTR = "baseUrl";

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
     *
     * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#LINKS_EXAMPLE}
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

    private Set<String> scopes;
    
    public ConnectAddonBean()
    {
        this.key = "";
        this.name = "";
        this.version = "1.0";
        this.description = "";
        this.vendor = VendorBean.newVendorBean().build();
        this.links = newHashMap();
        this.lifecycle = LifecycleBean.newLifecycleBean().build();
        this.modules = new ModuleList();
        this.scopes = new HashSet<String>();
        this.baseUrl = "";
        this.authentication = newAuthenticationBean().build();
        this.enableLicensing = null;
    }

    public ConnectAddonBean(ConnectAddonBeanBuilder builder)
    {
        super(builder);

        if (null == key)
        {
            this.key = "";
        }

        if (null == name)
        {
            this.name = "";
        }

        if (null == version)
        {
            this.version = "1.0";
        }

        if (null == description)
        {
            this.description = "";
        }

        if (null == modules)
        {
            this.modules = new ModuleList();
        }

        if (null == vendor)
        {
            this.vendor = VendorBean.newVendorBean().build();
        }

        if (null == links)
        {
            this.links = newHashMap();
        }

        if (null == scopes)
        {
            this.scopes = new HashSet<String>();
        }
        
        if (null == lifecycle)
        {
            this.lifecycle = LifecycleBean.newLifecycleBean().build();
        }
        if (null == baseUrl)
        {
            this.baseUrl = "";
        }
        if (null == authentication)
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

    public Set<ScopeName> getScopes()
    {
        // I would make the data member a Set of ScopeNames but gson sets bad scope names to null.
        return new HashSet<ScopeName>(transform(scopes, new Function<String, ScopeName>(){

            @Override
            public ScopeName apply(@Nullable String input)
            {
                if (null == input)
                {
                    throw new IllegalArgumentException("Scope names must not be null");
                }

                try
                {
                    return ScopeName.valueOf(input.toUpperCase());
                }
                catch (IllegalArgumentException e)
                {
                    throw new IllegalArgumentException(String.format("Unknown scope name '%s'", input), e);
                }
            }
        }));
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
                .append(scopes, other.scopes)
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
                .append(scopes)
                .build();
    }
}
