package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.LifecycleBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Allows an addon to register callbacks for plugin lifecycle events.
 * @schemaTitle Lifecycle
 */
public class LifecycleBean extends BaseCapabilityBean
{
    private String installed;
    private String uninstalled;
    private String enabled;
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
        
        if(null == installed)
        {
            this.installed = "";
        }
        if(null == uninstalled)
        {
            this.uninstalled = "";
        }
        if(null == enabled)
        {
            this.enabled = "";
        }
        if(null == disabled)
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
