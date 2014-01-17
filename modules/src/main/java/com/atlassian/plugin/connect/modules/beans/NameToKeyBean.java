package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.NameToKeyBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.cleanKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.nameToKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.randomName;

/**
 * @since 1.0
 */
public class NameToKeyBean extends BaseModuleBean
{
    /**
     * An OPTIONAL key to identify this module.
     * This key must be unique relative to the add on.
     * 
     * For most modules a key does not have to be specified as a random key will be assigned for each module.
     * A key only needs to be specified when it needs to be known for use in other modules or content.
     * 
     * For example, if you need to create a page and have the content of the page link to iteself or another page module
     * you'll need to specify the key(s) for the page(s) so that you can determine the url to link to.
     * 
     * All specified keys will have all special characters and spaces replaced with dashes and will be lower cased.
     * 
     * example: "MyAddon Key" will become "myaddon-key"
     */
    private String key;

    /**
     * A human-readable name
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

        if (null == name)
        {
            this.name = I18nProperty.empty();
        }
        if (null == key)
        {
            this.key = "";
        }
    }

    public String getKey()
    {
        if (!Strings.isNullOrEmpty(key))
        {
            return cleanKey(key);
        }

        return randomName("acmodule-");
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
        return (!Strings.isNullOrEmpty(getName().getValue()) ? getName().getValue() : getKey());
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
