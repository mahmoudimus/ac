package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.NameToKeyBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator.nameToKey;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @since 1.0
 */
public class NameToKeyBean extends BaseModuleBean
{
    private transient String key;

    /**
     * A human-readble name
     */
    @Required
    private I18nProperty name;

    public NameToKeyBean()
    {
        this.name = I18nProperty.empty();
        this.key = "";
    }

    public NameToKeyBean(NameToKeyBeanBuilder builder)
    {
        super(builder);

        if(null == name)
        {
            this.name = I18nProperty.empty();
        }
        if(null == key)
        {
            this.key = "";
        }
    }

    public String getKey()
    {
        if(!Strings.isNullOrEmpty(key))
        {
            return key;
        }
        
        return nameToKey(name.getValue());
    }

    public I18nProperty getName()
    {
        return name;
    }
    
    public String getRawKey()
    {
        return key;
    }

    public String getDisplayName()
    {
        return (!isNullOrEmpty(getName().getValue()) ? getName().getValue() : getKey());
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof NameToKeyBean))
        {
            return false;
        }

        NameToKeyBean other = (NameToKeyBean) otherObj;

        return new EqualsBuilder()
                .append(name, other.name)
                .append(key, other.key)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(51, 13)
                .append(key)
                .append(name)
                .build();
    }
}
