package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.VendorBeanBuilder;

import com.google.common.base.Objects;

/**
 * @since 1.0
 */
public class VendorBean extends BaseCapabilityBean
{
    private String name;
    private String url;

    public VendorBean()
    {
        this.name = "";
        this.url = "";
    }

    public VendorBean(VendorBeanBuilder builder)
    {
        super(builder);
        
        if(null == name)
        {
            this.name = "";
        }
        
        if(null == url)
        {
            this.url = "";
        }
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }
    
    public static VendorBeanBuilder newVendorBean()
    {
        return new VendorBeanBuilder();
    }

    public static VendorBeanBuilder newVendorBean(VendorBean defaultBean)
    {
        return new VendorBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name,url);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof VendorBean))
        {
            return false;
        }
        else
        {
            final VendorBean that = (VendorBean) obj;
            return Objects.equal(name, that.name) &&
                    Objects.equal(url, that.url);
        }
    }
}
