package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.GeneratedKeyBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.cleanKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.randomName;

/**
 * @since 1.0
 */
public class GeneratedKeyBean extends NamedBean
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
     * example: "My Addon Key" will become "my-addon-key"
     */
    private String key;

    private transient String calculatedKey;

    public GeneratedKeyBean()
    {
        this.key = "";
    }

    public GeneratedKeyBean(GeneratedKeyBeanBuilder builder)
    {
        super(builder);

        if (null == key)
        {
            this.key = "";
        }
    }

    public String getKey()
    {
        if(Strings.isNullOrEmpty(calculatedKey))
        {
            if (!Strings.isNullOrEmpty(key))
            {
                this.calculatedKey = cleanKey(key);
            }
            else
            {
                this.calculatedKey = randomName("acmodule-");
            }
        }

        return calculatedKey;
    }

    public String getRawKey()
    {
        return key;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof GeneratedKeyBean && super.equals(otherObj)))
        {
            return false;
        }

        GeneratedKeyBean other = (GeneratedKeyBean) otherObj;

        return new EqualsBuilder()
                .append(key, other.key)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(101, 59)
                .append(super.hashCode())
                .append(key)
                .build();
    }
}
