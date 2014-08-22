package com.atlassian.plugin.connect.modules.beans.builder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedList;


public abstract class ModuleListBuilder<T extends ModuleListBuilder,
        B extends ModuleList> extends BaseModuleBeanBuilder<T, B>
{
    public T withModules(String fieldName, ModuleBean... beans)
    {
        for (ModuleBean bean : beans)
        {
            withModule(fieldName, bean);
        }
        return (T) this;
    }

    public T withModule(String fieldName, ModuleBean bean)
    {
//        if (null == modules)
//        {
//            this.modules = new ModuleList();
//        }

        addBeanReflectivelyByType(fieldName, bean);

        return (T) this;
    }

    // subclasses must override
//    public B build()
//    {
//        return (B) new ModuleList(this);
//    }


    private void addBeanReflectivelyByType(String fieldName, ModuleBean bean)
    {
        Class beanClass = bean.getClass();
        try
        {
            Field field = getClass().getDeclaredField(fieldName);
            Type fieldType = field.getGenericType();

            if (fieldType.equals(beanClass))
            {
                field.setAccessible(true);
                field.set(this, bean);
            }
            else if (isParameterizedList(fieldType))
            {
                Type listType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (listType.equals(beanClass))
                {
                    field.setAccessible(true);
                    List beanList = (List) field.get(this);
                    beanList.add(bean);
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to access module field for bean of type: " + bean.getClass(), e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Unable to find module field '" + fieldName + "' for bean of type: " + bean.getClass());
        }
    }

}
