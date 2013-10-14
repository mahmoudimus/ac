package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

/**
 * @since version
 */
public class IconBeanBuilder<T extends IconBeanBuilder, B extends IconBean>
{
    private int width;
    private int height;
    private String url;

    public IconBeanBuilder()
    {
    }

    public IconBeanBuilder(IconBean defaultBean)
    {
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
        this.url = defaultBean.getUrl();
    }

    public IconBeanBuilder withWidth(int width)
    {
        this.width = width;
        return this;
    }

    public IconBeanBuilder withHeight(int height)
    {
        this.height = height;
        return this;
    }

    public IconBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public B build()
    {
        return (B) new IconBean(this);
    }
}
