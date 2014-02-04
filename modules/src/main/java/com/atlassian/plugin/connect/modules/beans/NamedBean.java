package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.NamedBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @since 1.0
 */
public class NamedBean extends BaseModuleBean
{
    /**
     * A human readable name.
     */
    @Required
    private I18nProperty name;

    public NamedBean()
    {
        this.name = I18nProperty.empty();
    }

    public NamedBean(final NamedBeanBuilder builder)
    {
        super(builder);

        if (null == name)
        {
            this.name = I18nProperty.empty();
        }
    }

    public I18nProperty getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return getName().getValue();
    }

    // don't call super because BaseModuleBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof NamedBean))
        {
            return false;
        }

        NamedBean other = (NamedBean) otherObj;

        return new EqualsBuilder()
                .append(name, other.name)
                .isEquals();
    }

    // don't call super because BaseModuleBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(67, 41)
                .append(name)
                .build();
    }

}

