package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.IconCapabilityBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.CapabilityBeanUtils.copyFieldsByNameAndType;

/**
 * @since version
 */
public class IconCapabilityBean
{
    private int width;
    private int height;
    private String url;

    public IconCapabilityBean()
    {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public IconCapabilityBean(IconCapabilityBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);

        if (null == url)
        {
            this.url = "";
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public String getUrl()
    {
        return url;
    }

    public static IconCapabilityBeanBuilder newIconCapabilityBean()
    {
        return new IconCapabilityBeanBuilder();
    }

    public static IconCapabilityBeanBuilder newIconCapabilityBean(IconCapabilityBean defaultBean)
    {
        return new IconCapabilityBeanBuilder(defaultBean);
    }

}
