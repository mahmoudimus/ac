package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.api.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.CapabilityBeanUtils.copyFieldsByNameAndType;

/**
 * @since version
 */
public class BaseCapabilityBean implements CapabilityBean
{
    protected String key;
    protected I18nProperty name;
    protected I18nProperty description;

    protected BaseCapabilityBean()
    {
        this.key = key;
        this.name = new I18nProperty("", "");
        this.description = new I18nProperty("", "");
    }

    public BaseCapabilityBean(BaseCapabilityBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);

        //these may have been set by child class
        if (null == key)
        {
            this.key = "";
        }

        if (null == name)
        {
            this.name = new I18nProperty("", "");
        }

        if (null == description)
        {
            this.description = new I18nProperty("", "");
        }
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public I18nProperty getName()
    {
        return name;
    }

    @Override
    public I18nProperty getDescription()
    {
        return description;
    }

}
