package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.RequiredKeyBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.cleanKey;
import static com.google.common.base.Preconditions.checkState;

/**
 * @since 1.0
 */
public class RequiredKeyBean extends NamedBean
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
    @Required
    @StringSchemaAttributes(pattern = "^[a-zA-Z0-9-]+$")
    private String key;

    private transient String calculatedKey;

    public RequiredKeyBean()
    {
        this.key = "";
    }

    public RequiredKeyBean(final RequiredKeyBeanBuilder builder)
    {
        super(builder);

        if (null == key)
        {
            this.key = "";
        }
    }

    public String getKey(ConnectAddonBean addon)
    {
        checkState(!Strings.isNullOrEmpty(key),"Modules cannot contain empty or null keys.");
        if (Strings.isNullOrEmpty(calculatedKey))
        {
            this.calculatedKey = addonAndModuleKey(addon.getKey(),key);
        }
        return calculatedKey;
    }

    public String getRawKey()
    {
        return cleanKey(key);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof RequiredKeyBean && super.equals(otherObj)))
        {
            return false;
        }

        RequiredKeyBean other = (RequiredKeyBean) otherObj;

        return new EqualsBuilder()
                .append(key, other.key)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 37)
                .append(super.hashCode())
                .append(key)
                .build();
    }

}
