package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;

/**
 * @since 1.0
 */
public class VendorBeanBuilder<T extends VendorBeanBuilder, B extends VendorBean> extends BaseModuleBeanBuilder<T, B>
{
    private String name;
    private String url;

    public VendorBeanBuilder()
    {
    }

    public VendorBeanBuilder(VendorBean defaultBean)
    {
        this.name = defaultBean.getName();
        this.url = defaultBean.getUrl();
    }

    public T withName(String name)
    {
        this.name = name;
        return (T) this;
    }

    public T withUrl(String url)
    {
        this.url = url;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new VendorBean(this);
    }
}
