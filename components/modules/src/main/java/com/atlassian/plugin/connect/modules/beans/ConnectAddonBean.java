package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectAddonBean extends ShallowConnectAddonBean
{
    /**
     * The list of modules this add-on provides
     */
    private Map<String, Supplier<List<ModuleBean>>> modules;

    /**
     * A validity-aware abstraction of the list of modules this add-on provides
     */
    private transient Supplier<ModuleMultimap> moduleListMap = Suppliers.memoize(new Supplier<ModuleMultimap>()
    {

        @Override
        public ModuleMultimap get()
        {
            return new ModuleMultimap(modules);
        }
    });

    public ConnectAddonBean()
    {
        this.modules = new HashMap<>();
    }

    public ConnectAddonBean(ConnectAddonBeanBuilder builder)
    {
        super(builder);
        if (null == modules)
        {
            this.modules = new HashMap<>();
        }
    }

    public static ConnectAddonBeanBuilder newConnectAddonBean()
    {
        return new ConnectAddonBeanBuilder();
    }
    
    public static ConnectAddonBeanBuilder newConnectAddonBean(ShallowConnectAddonBean defaultBean)
    {
        return new ConnectAddonBeanBuilder(defaultBean);
    }

    /**
     * Returns a map of module types to lists of modules.
     *
     * @return the module list map
     */
    public ModuleMultimap getModules()
    {
        return moduleListMap.get();
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (!(otherObj instanceof ConnectAddonBean))
        {
            return false;
        }
        ConnectAddonBean other = (ConnectAddonBean) otherObj;
        
        return new EqualsBuilder().append(getModules(), other.getModules()).isEquals() && super.equals(otherObj);
    }
    
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(41, 7).appendSuper(super.hashCode()).build();
    }
}
