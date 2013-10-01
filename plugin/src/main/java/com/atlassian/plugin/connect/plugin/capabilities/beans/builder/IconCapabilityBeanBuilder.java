package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.IconCapabilityBean;

/**
 * @since version
 */
public class IconCapabilityBeanBuilder<T extends IconCapabilityBeanBuilder, B extends IconCapabilityBean>
{
    private int width;
    private int height;
    private String url;

    public IconCapabilityBeanBuilder()
    {
    }

    public IconCapabilityBeanBuilder(IconCapabilityBean defaultBean)
    {
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
        this.url = defaultBean.getUrl();
    }

    public IconCapabilityBeanBuilder withWidth(int width)
    {
        this.width = width;
        return this;
    }

    public IconCapabilityBeanBuilder withHeight(int height)
    {
        this.height = height;
        return this;
    }

    public IconCapabilityBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public B build()
    {
        return (B) new IconCapabilityBean(this);
    }
}
