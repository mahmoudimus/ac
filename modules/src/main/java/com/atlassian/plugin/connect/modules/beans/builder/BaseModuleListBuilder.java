package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BaseModuleList;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedList;

public class BaseModuleListBuilder<T extends BaseModuleListBuilder,
        M extends BaseModuleList> extends BaseModuleBeanBuilder<T, M>
{
//    protected M modules;   // subclasses must override
//
//
//    public M build()
//    {
//        return modules;
//    }
//
//
//
//    private void addBeanReflectivelyByType(String fieldName, ModuleBean bean)
//    {
//        Class beanClass = bean.getClass();
//        try
//        {
//            final Field field = FieldUtils.getField(modules.getClass(), fieldName, true);
//            Type fieldType = field.getGenericType();
//
//            if (fieldType.equals(beanClass))
//            {
//                field.setAccessible(true);
//                field.set(modules, bean);
//            }
//            else if (isParameterizedList(fieldType))
//            {
//                Type listType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
//                if (listType.equals(beanClass))
//                {
//                    field.setAccessible(true);
//                    List beanList = (List) field.get(modules);
//                    beanList.add(bean);
//                }
//            }
//        }
//        catch (IllegalAccessException e)
//        {
//            throw new RuntimeException("Unable to access module field for bean of type: " + bean.getClass(), e);
//        }
//    }

}
