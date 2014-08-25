package com.atlassian.plugin.connect.modules.beans.builder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import org.apache.commons.lang3.reflect.FieldUtils;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedList;


public abstract class ModuleListBuilder<T extends ModuleListBuilder,
        M extends ModuleList> extends BaseModuleBeanBuilder<T, M>
{
    private M modules;

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
        if (null == modules)
        {
            this.modules = createEmpty();
        }

        addBeanReflectivelyByType(fieldName, bean);

        return (T) this;
    }

    protected abstract M createEmpty();

    public abstract M build();
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
//            FieldUtils.writeField(modules, fieldName, bean, true);
            final Field field = FieldUtils.getField(modules.getClass(), fieldName, true);
            Type fieldType = field.getGenericType();

            if (fieldType.equals(beanClass))
            {
                field.setAccessible(true);
                field.set(modules, bean);
            }
            else if (isParameterizedList(fieldType))
            {
                Type listType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (listType.equals(beanClass))
                {
                    field.setAccessible(true);
                    List beanList = (List) field.get(modules);
                    beanList.add(bean);
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to access module field for bean of type: " + bean.getClass(), e);
        }
//        catch (NoSuchFieldException e)
//        {
//            throw new RuntimeException("Unable to find module field '" + fieldName + "' for bean of type: " + bean.getClass());
//        }
    }

}
