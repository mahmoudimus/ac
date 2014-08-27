package com.atlassian.plugin.connect.modules.beans.builder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import org.apache.commons.lang3.reflect.FieldUtils;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedList;


public class ModuleListBuilder<T extends ModuleListBuilder,
        M extends ModuleList> extends BaseModuleBeanBuilder<T, M>
{
    protected M modules;

    public T withWebItems(WebItemModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("webItems", beans);
    }

    public T withWebPanels(WebPanelModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("webPanels", beans);
    }

    public T withWebSections(WebSectionModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("webSections", beans);
    }

    public T withWebHooks(WebHookModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("webhooks", beans);
    }

    public T withGeneralPages(ConnectPageModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("generalPages", beans);
    }

    public T withAdminPages(ConnectPageModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("adminPages", beans);
    }

    public T withProfilePages(ConnectPageModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("profilePages", beans);
    }

    public T withConfigurePage(ConnectPageModuleBean bean)
    {
        // TODO: temp impl until withModules removed
        return withModule("configurePage", bean);
    }



    @Deprecated // use explicit methods like withWebHooks
    public T withModules(String fieldName, ModuleBean... beans)
    {
        for (ModuleBean bean : beans)
        {
            withModule(fieldName, bean);
        }
        return (T) this;
    }

    @Deprecated // use explicit methods like withWebItems
    public T withModule(String fieldName, ModuleBean bean)
    {
        if (null == modules)
        {
            this.modules = createEmpty();
        }

        addBeanReflectivelyByType(fieldName, bean);

        return (T) this;
    }

    // subclasses must override
    protected M createEmpty()
    {
        return (M) new ModuleList();
    }

    // subclasses must override
    public M build()
    {
        return modules;
    }



    private void addBeanReflectivelyByType(String fieldName, ModuleBean bean)
    {
        Class beanClass = bean.getClass();
        try
        {
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
    }

}
