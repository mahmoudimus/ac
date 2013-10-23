package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @since version
 */
public class ConnectAddonBeanBuilder<T extends ConnectAddonBeanBuilder, B extends ConnectAddonBean> extends BaseCapabilityBeanBuilder<T,B>
{
    private String key;
    private String name;
    private String version;
    private VendorBean vendor;
    private Map<String,String> links;
    private Map<String,List<? extends CapabilityBean>> capabilities;

    public ConnectAddonBeanBuilder()
    {
    }

    public ConnectAddonBeanBuilder(ConnectAddonBean defaultBean)
    {
        this.key = defaultBean.getKey();
        this.name = defaultBean.getName();
        this.version = defaultBean.getVersion();
        this.vendor = defaultBean.getVendor();
        this.links = defaultBean.getLinks();
        this.capabilities = defaultBean.getCapabilities();
    }

    public T withKey(String key)
    {
        this.key = key;
        return (T) this;
    }
    
    public T withName(String name)
    {
        this.name = name;
        return (T) this;
    }

    public T withVersion(String version)
    {
        this.version = version;
        return (T) this;
    }

    public T withVendor(VendorBean vendor)
    {
        this.vendor = vendor;
        return (T) this;
    }
    
    public T withCapabilities(Map<String,List<? extends CapabilityBean>> capabilities)
    {
        this.capabilities = capabilities;
        return (T) this;
    }

    public T withCapabilities(List<CapabilityBean> beans)
    {
        if(null == capabilities)
        {
            this.capabilities = newHashMap();
        }
        
        String key = beans.get(0).getClass().getAnnotation(CapabilitySet.class).key();
        
        capabilities.put(key,beans);
        
        return (T) this;
    }

    public T withCapabilities(CapabilityBean ... beans)
    {
        if(null == capabilities)
        {
            this.capabilities = newHashMap();
        }

        String key = beans[0].getClass().getAnnotation(CapabilitySet.class).key();

        capabilities.put(key, Arrays.asList(beans));

        return (T) this;
    }
    
    public T withCapability(CapabilityBean bean)
    {
        if(null == capabilities)
        {
            this.capabilities = newHashMap();
        }
        String key = bean.getClass().getAnnotation(CapabilitySet.class).key();
        
        if(capabilities.containsKey(key))
        {
            List<CapabilityBean> beanList = (List<CapabilityBean>) capabilities.get(key);
            beanList.add(bean);
        }
        else
        {
            capabilities.put(bean.getClass().getAnnotation(CapabilitySet.class).key(), newArrayList(bean));
        }
        
        return (T) this;
    }

    public T withLinks(Map<String,String> links)
    {
        this.links = links;

        return (T) this;
    }

    public B build()
    {
        return (B) new ConnectAddonBean(this);
    }
}
