package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.GeneratedKeyBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.cleanKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;

/**
 * @since 1.0
 */
public class GeneratedKeyBean extends NamedBean
{
    /**
     * A key to identify this module.
     * This key must be unique relative to the add on.
     * 
     * All specified keys will have all special characters and spaces replaced with dashes and will be lower cased.
     * 
     * example: "My Addon Key" will become "my-addon-key"
     *
     * The key is used to generate the url to your add-on's module. The url is generated as a combination of your add-on
     * key and module key. For example, an add-on which looks like:
     *
     *    {
     *        "key": "my-addon",
     *        "modules": {
     *            "configurePage": {
     *                "key": "configure-me",
     *            }
     *        }
     *    }
     *
     * Will have a configuration page module with a URL of `/plugins/servlet/ac/my-addon/configure-me`.
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
