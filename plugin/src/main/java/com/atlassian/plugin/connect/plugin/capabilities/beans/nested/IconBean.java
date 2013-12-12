package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.IconBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleBeanUtils.copyFieldsByNameAndType;

/**
 * @since 1.0
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

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof IconBean))
        {
            return false;
        }

        IconBean other = (IconBean) otherObj;

        return new EqualsBuilder()
                .append(width, other.width)
                .append(height, other.height)
                .append(url, other.url)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(53, 11)
                .append(width)
                .append(height)
                .append(url)
                .build();
    }
}
