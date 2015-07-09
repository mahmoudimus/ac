package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.LifecycleBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Allows an add-on to register callbacks for plugin lifecycle events. Each property in this object is a URL relative to
 * the add-on's base URL. When a lifecycle event is fired, it will POST to the appropriate URL registered for the event.
 *
 *#### Lifecycle Attribute Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#LIFECYCLE_EXAMPLE}
 *
 *#### Lifecycle Payload
 *Lifecycle callbacks contain a JSON data payload with important tenant information that you will need to store in your
 *  add-on in order to sign and verify future requests. The payload contains the following attributes:
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#LIFECYCLE_PAYLOAD_EXAMPLE}
 *
 *<table summary="Lifecycle payload attributes" class='aui'>
 *    <thead>
 *        <tr>
 *            <th>Attribute</th>
 *            <th>Description</th>
 *        </tr>
 *    </thead>
 *    <tr>
 *        <td><code>key</code></td>
 *        <td>Add-on key that was installed into the Atlassian Product, as it appears in your add-on's descriptor.</td>
 *    </tr>
 *    <tr>
 *        <td><code>clientKey</code></td>
 *        <td>Identifying key for the Atlassian product instance that the add-on was installed into. This will never change for a given
 *        instance, and is unique across all Atlassian product tenants. This value should be used to key tenant details
 *        in your add-on.</td>
 *    </tr>
 *    <tr>
 *        <td><code>publicKey</code></td>
 *        <td>This is the public key for this Atlassian product instance. You may verify that this <code>baseUrl</code>
 * 			uses this <code>publicKey</code> at the standard URL <code>&lt;baseUrl&gt;/plugins/servlet/oauth/consumer-info</code>.</td>
 *    </tr>
 *    <tr>
 *        <td><code>sharedSecret</code></td>
 *        <td>Use this string to sign outgoing JWT tokens and validate incoming JWT tokens. Optional: and may not
 *        be present on non-JWT add-on installations, and is only sent on the <code>installed</code> event.</td>
 *    </tr>
 *    <tr>
 *        <td><code>serverVersion</code></td>
 *        <td>This is a string representation of the host product's version. Generally you should not need it.</td>
 *    </tr>
 *    <tr>
 *        <td><code>pluginsVersion</code></td>
 *        <td>This is a semver compliant version of Atlassian Connect which is running on the host server, for example: <code>1.1.15</code>.</td>
 *    </tr>
 *    <tr>
 *        <td><code>baseUrl</code></td>
 *        <td>URL prefix for this Atlassian product instance. All of its REST endpoints begin with this `baseUrl`.</td>
 *    </tr>
 *    <tr>
 *        <td><code>productType</code></td>
 *        <td>Identifies the category of Atlassian product, e.g. <code>jira</code> or <code>confluence</code>.</td>
 *    </tr>
 *    <tr>
 *        <td><code>description</code></td>
 *        <td>The host product description - this is customisable by an instance administrator.</td>
 *    </tr>
 *    <tr>
 *        <td><code>serviceEntitlementNumber</code></td>
 *        <td>Also known as the SEN, the service entitlement number is a the add-on license id.</td>
 *    </tr>
 *</table>
 *
 * @schemaTitle Lifecycle
 */
public class LifecycleBean extends BaseModuleBean
{
    /**
     * When a Connect add-on is installed, a synchronous request is fired to this URL to initiate the installation
     * handshake. In order to successfully complete installation, the add-on must respond with either a `200 OK` or
     * `204 No Content` status.
     *<div class="aui-message warning">
     *    <p class="title">
     *        <span class="aui-icon icon-warning"></span>
     *        <strong>Important</strong>
     *    </p>
     *    Upon successful registration, the add-on must return either a `200 OK` or `204 No Content` response code, otherwise
     *    the operation will fail and the installation will be marked as incomplete.
     *</div>
     */
    @StringSchemaAttributes(format = "uri")
    private String installed;

    /**
     * Fires when an add on has been successfully un-installed. This is an asynchronous notification event.
     */
    @StringSchemaAttributes(format = "uri")
    private String uninstalled;

    /**
     * Fires when an add on has been successfully enabled. This is an asynchronous notification event.
     */
    @StringSchemaAttributes(format = "uri")
    private String enabled;

    /**
     * Fires when an add on has been successfully disabled. This is an asynchronous notification event.
     */
    @StringSchemaAttributes(format = "uri")
    private String disabled;

    public LifecycleBean()
    {
        this.installed = "";
        this.uninstalled = "";
        this.enabled = "";
        this.disabled = "";
    }

    public LifecycleBean(LifecycleBeanBuilder builder)
    {
        super(builder);

        if (null == installed)
        {
            this.installed = "";
        }
        if (null == uninstalled)
        {
            this.uninstalled = "";
        }
        if (null == enabled)
        {
            this.enabled = "";
        }
        if (null == disabled)
        {
            this.disabled = "";
        }
    }

    public String getInstalled()
    {
        return installed;
    }

    public String getUninstalled()
    {
        return uninstalled;
    }

    public String getEnabled()
    {
        return enabled;
    }

    public String getDisabled()
    {
        return disabled;
    }

    public boolean isEmpty()
    {
        return (Strings.isNullOrEmpty(installed) && Strings.isNullOrEmpty(uninstalled) && Strings.isNullOrEmpty(enabled) && Strings.isNullOrEmpty(disabled));
    }

    public static LifecycleBeanBuilder newLifecycleBean()
    {
        return new LifecycleBeanBuilder();
    }

    public static LifecycleBeanBuilder newLifecycleBean(LifecycleBean defaultBean)
    {
        return new LifecycleBeanBuilder(defaultBean);
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof LifecycleBean))
        {
            return false;
        }

        LifecycleBean other = (LifecycleBean) otherObj;

        return new EqualsBuilder()
                .append(installed, other.installed)
                .append(uninstalled, other.uninstalled)
                .append(enabled, other.enabled)
                .append(disabled, other.disabled)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(37, 11)
                .append(installed)
                .append(uninstalled)
                .append(enabled)
                .append(disabled)
                .build();
    }
}
