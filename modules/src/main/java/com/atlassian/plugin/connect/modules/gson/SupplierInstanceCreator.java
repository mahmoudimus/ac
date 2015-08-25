package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.google.common.base.Supplier;
import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by jfurler on 24/08/2015.
 */
public class SupplierInstanceCreator implements InstanceCreator<Supplier<List<ModuleBean>>>
{
    
    public Supplier createInstance(Type type)
    {
        return new Supplier<List<ModuleBean>>()
        {
            @Override
            public List<ModuleBean> get()
            {
                return null;
                
            }
        };
        
    }
}
