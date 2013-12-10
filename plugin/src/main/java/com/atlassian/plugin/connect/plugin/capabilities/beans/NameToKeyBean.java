package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.NameToKeyBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

import com.google.common.base.Strings;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator.nameToKey;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @since 1.0
 */
public class NameToKeyBean extends BaseModuleBean
{
    private transient String key;
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
}
