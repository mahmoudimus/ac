package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;
import static com.google.common.collect.Maps.newHashMap;

/**
 * <p>The add-on descriptor is a JSON file (<code>atlassian-connect.json</code>) that describes the add-on to the Atlassian application.
 * The descriptor includes general information for the add-on, as well as the modules that the add-on wants to use or
 * extend.</p>
 *
 * <p>If you're familiar with Java add-on development with previous versions of the Atlassian Plugin Framework, you may already be
 * familiar with the `atlassian-plugin.xml` descriptors. The `atlassian-connect.json` serves the same function.</p>
 *
 * <p>The descriptor serves as the glue between the remote add-on and the Atlassian application. When an administrator for a
 * cloud instance installs an add-on, what they are really doing is installing this descriptor file, which
 * contains pointers to your service. You can see an example below.</p>
 *
 * <p>For details and application-specific reference information on the descriptor please refer to the "jira modules"
 * and "confluence modules" sections of this documentation. But we'll call out a few highlights from the example here.</p>
 *
 * <p>The version element identifies the version of the add-on itself. Note that versioning works differently for Atlassian
 * Connect add-ons than it does for traditional, in-process add-ons.</p>
 *
 * <p>Since Atlassian Connect add-ons are remote and largely independent from the Atlassian application, they can be changed
 * at any time, without having to create a new version or report the change to the Atlassian instance. The changes are
 * reflected in the Atlassian instance immediately (or at least at page reload time).</p>
 *
 * <p>However, some add-on changes do require a change in the descriptor file itself. For example, say you modify the add-on
 * to have a new page module. Since this requires a page module declaration in the descriptor, it means making an updated
 * descriptor available, which instances will have to re-register. To propagate this change, you need to create a new version
 * of the add-on in its Marketplace listing. The Marketplace will take care of the rest: informing administrators
 * and automatically installing the available update. See [Upgrades](../developing/upgrades.html) for more details.</p>
 *
 * <div class="aui-message aui-message-info">
 *     <p class="title">
 *         <strong>Validating your descriptor</strong>
 *     </p>
 *     <p>You can validate your descriptor using this <a href="https://atlassian-connect-validator.herokuapp.com/validate">handy tool</a>.</p>
 * </div>
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ADDON_EXAMPLE}
 * @schemaTitle Addon Descriptor
 * @since 1.0
 */
public class ShallowConnectAddonBean extends BaseModuleBean {
    public static final String KEY_ATTR = "key";
    public static final String BASE_URL_ATTR = "baseUrl";

    public static final int DEFAULT_WEIGHT = 100;

    public static final int MAX_KEY_LENGTH = 80;

    /**
     * A unique key to identify the add-on.
     * This key must be <= 80 characters.
     */
    @StringSchemaAttributes(maxLength = MAX_KEY_LENGTH, pattern = "^[a-zA-Z0-9-._]+$")
    @Required
    private String key;

    /**
     * The human-readable name of the add-on
     */
    private String name;

    /**
     * <b>NOTE</b> This field is reserved for Atlassian Marketplace. Any value provided will be ignored.
     *
     * The version of the add-on. Upon registration of a new add-on version in Atlassian Marketplace, a value for this
     * field is generated and subsequently provided to the Universal Plugin Manager.
     */
    private String version;

    /**
     * The API version is an OPTIONAL integer. If omitted we will infer an API version of 1.
     *
     * The intention behind the API version is to allow vendors the ability to beta test a major revision to their Connect add-on as a private version,
     * and have a seamless transition for those beta customers (and existing customers) once the major revision is launched. 
     *
     * Vendors can accomplish this by listing a new private version of their add-on, with a new descriptor hosted at a new URL. 
     *
     * They use the Atlassian Marketplace's access token facilities to share this version with customers (or for internal use). 
     * When this version is ready to be taken live, it can be transitioned from private to public, and all customers will be seamlessly updated.
     *
     * It's important to note that this approach allows vendors to create new versions manually, despite the fact that in the common case, the versions are automatically created.
     * This has a few benefits-- for example, it gives vendors the ability to change their descriptor URL if they need to 
     * (the descriptor URL will be immutable for existing versions)
     */
    private Integer apiVersion;

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
     * The base url of the remote add-on, which is used for all communications back to the add-on instance. Once the add-on is installed in a product, the add-on's baseUrl
     * cannot be changed without first uninstalling the add-on. This is important; choose your baseUrl wisely before making your add-on public.
     *
     * Only add-ons with a baseUrl starting with ``https://`` can be [installed in cloud instances](../developing/cloud-installation.html)
     * servers. ``http://`` may still be used for testing locally.
     *
     * Note: each add-on must have a unique baseUrl. If you would like to serve multiple add-ons from the same host, consider adding a path prefix into the baseUrl.
     */
    @Required
    @StringSchemaAttributes(format = "uri")
    private String baseUrl;

    /**
     * Defines the authentication type to use when signing requests between the host application and the connect add on.
     */
    @Required
    private AuthenticationBean authentication;

    /**
     * Whether or not to enable licensing options in the UPM/Marketplace for this add on
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean enableLicensing;

    /**
     * Set of [scopes](../scopes/scopes.html) requested by this add on
     *
     * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#SCOPES_EXAMPLE}
     */
    private Set<ScopeName> scopes;

    /**
     * The list of modules this add-on provides.
     */
    private Map<String, ?> modules; // Only used for shallow schema generation

    public ShallowConnectAddonBean() {
        this.key = "";
        this.name = "";
        this.version = "1.0";
        this.description = "";
        this.vendor = VendorBean.newVendorBean().build();
        this.links = newHashMap();
        this.lifecycle = LifecycleBean.newLifecycleBean().build();
        this.scopes = new HashSet<>();
        this.baseUrl = "";
        this.authentication = newAuthenticationBean().build();
        this.enableLicensing = null;
    }

    public ShallowConnectAddonBean(ConnectAddonBeanBuilder builder) {
        copyFieldsByNameAndType(builder, this);

        if (null == key) {
            this.key = "";
        }

        if (null == name) {
            this.name = "";
        }

        if (null == version) {
            this.version = "1.0";
        }

        if (null == description) {
            this.description = "";
        }

        if (null == vendor) {
            this.vendor = VendorBean.newVendorBean().build();
        }

        if (null == links) {
            this.links = newHashMap();
        }

        if (null == scopes) {
            this.scopes = new HashSet<>();
        }

        if (null == lifecycle) {
            this.lifecycle = LifecycleBean.newLifecycleBean().build();
        }
        if (null == baseUrl) {
            this.baseUrl = "";
        }
        if (null == authentication) {
            this.authentication = newAuthenticationBean().build();
        }
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Integer getApiVersion() {
        return (null != apiVersion && apiVersion > 0) ? apiVersion : 1;
    }

    public String getDescription() {
        return description;
    }

    public VendorBean getVendor() {
        return vendor;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public Set<ScopeName> getScopes() {
        return scopes;
    }

    public LifecycleBean getLifecycle() {
        return lifecycle;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public AuthenticationBean getAuthentication() {
        return authentication;
    }

    public Boolean getEnableLicensing() {
        return (null != enableLicensing) ? enableLicensing : Boolean.FALSE;
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == this) {
            return true;
        }

        if (!(otherObj instanceof ShallowConnectAddonBean)) {
            return false;
        }

        ShallowConnectAddonBean other = (ShallowConnectAddonBean) otherObj;

        return new EqualsBuilder()
                .append(key, other.key)
                .append(name, other.name)
                .append(version, other.version)
                .append(apiVersion, other.apiVersion)
                .append(description, other.description)
                .append(vendor, other.vendor)
                .append(links, other.links)
                .append(lifecycle, other.lifecycle)
                .append(baseUrl, other.baseUrl)
                .append(authentication, other.authentication)
                .append(enableLicensing, other.enableLicensing)
                .append(scopes, other.scopes)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 7)
                .append(key)
                .append(name)
                .append(version)
                .append(apiVersion)
                .append(description)
                .append(vendor)
                .append(links)
                .append(lifecycle)
                .append(baseUrl)
                .append(authentication)
                .append(enableLicensing)
                .append(scopes)
                .build();
    }
}
