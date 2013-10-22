package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean;

/**
 * @since version
 */
public class VendorBeanBuilder<T extends VendorBeanBuilder, B extends VendorBean> extends BaseCapabilityBeanBuilder<T,B>
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
    
    public B build()
    {
        return (B) new VendorBean(this);
    }
}
