package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.LifecycleBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Allows an add-on to register callbacks for plugin lifecycle events. Each property in this object is a url that can be
 * absolute or relative to the add-on's baseUrl. When a lifecycle event is fired, it will POST to the appropriate url
 * registered for the event.
 * <p/>
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#LIFECYCLE_EXAMPLE}
 * @schemaTitle Lifecycle
 */
public class LifecycleBean extends BaseModuleBean
{
    /**
     * When a Connect add-on is installed, a synchronous request is fired to this URL to initiate the installation
     * handshake. In order to successfully complete installation, the add-on must respond with either a `200 OK` or
     * `204 No Content` status.
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
