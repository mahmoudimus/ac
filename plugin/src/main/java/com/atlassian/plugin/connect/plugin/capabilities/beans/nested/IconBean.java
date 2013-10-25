package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.IconBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.CapabilityBeanUtils.copyFieldsByNameAndType;

/**
 * @since 1.0
 */
public class IconBean
{
    private int width;
    private int height;
    private String url;

    public IconBean()
    {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public IconBean(IconBeanBuilder builder)
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

    public static IconBeanBuilder newIconBean()
    {
        return new IconBeanBuilder();
    }

    public static IconBeanBuilder newIconBean(IconBean defaultBean)
    {
        return new IconBeanBuilder(defaultBean);
    }

}
