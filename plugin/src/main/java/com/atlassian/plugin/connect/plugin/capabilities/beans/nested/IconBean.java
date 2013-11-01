package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.IconBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.CapabilityBeanUtils.copyFieldsByNameAndType;

/**
 * @since version
 */
public class IconBean
{
    /**
     * The width in pixels of the icon image.
     */
    private int width;
    /**
     * The height in pixels of the icon image.
     */
    private int height;
    /**
     * The URL of the icon. Your icon needs to be hosted remotely at a web-accessible location. You can specify the
     * URL as an absolute URL or relative to the add-on's base URL.
     */
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
