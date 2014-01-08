package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.beans.*;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ConnectReflectionHelper.isParameterizedList;

/**
 * @since 1.0
 */
@SuppressWarnings ({ "unchecked", "UnusedDeclaration" })
public class ConnectAddonBeanBuilder<T extends ConnectAddonBeanBuilder, B extends ConnectAddonBean> extends BaseModuleBeanBuilder<T, B>
{
    private String key;
    private String name;
    private String version;
    private String description;
    private VendorBean vendor;
    private Map<String, String> links;
    private ModuleList modules;
    private Set<ScopeName> scopes;
    private LifecycleBean lifecycle;
    private String baseUrl;
    private AuthenticationBean authentication;
    private Boolean enableLicensing;

    public ConnectAddonBeanBuilder()
    {
    }

    public ConnectAddonBeanBuilder(ConnectAddonBean defaultBean)
    {
        this.key = defaultBean.getKey();
        this.name = defaultBean.getName();
        this.version = defaultBean.getVersion();
        this.description = defaultBean.getDescription();
        this.vendor = defaultBean.getVendor();
        this.links = defaultBean.getLinks();
        this.modules = defaultBean.getModules();
        this.lifecycle = defaultBean.getLifecycle();
        this.baseUrl = defaultBean.getBaseUrl();
        this.authentication = defaultBean.getAuthentication();
        this.scopes = defaultBean.getScopes();
        this.enableLicensing = defaultBean.getEnableLicensing();
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

    public T withDescription(String description)
    {
        this.description = description;
        return (T) this;
    }

    public T withVendor(VendorBean vendor)
    {
        this.vendor = vendor;
        return (T) this;
    }

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
            this.modules = new ModuleList();
        }

        addBeanReflectivelyByType(fieldName, modules, bean);

        return (T) this;
    }

    public T withLinks(Map<String, String> links)
    {
        this.links = links;

        return (T) this;
    }

    public T withScopes(Set<ScopeName> scopes)
    {
        if (null == this.scopes)
        {
            this.scopes = new HashSet<ScopeName>(scopes.size());
        }
        else
        {
            this.scopes.clear();
        }

        this.scopes.addAll(scopes);
        return (T) this;
    }
    
    public T withLifecycle(LifecycleBean lifecycle)
    {
        this.lifecycle = lifecycle;
        return (T) this;
    }

    public T withBaseurl(String url)
    {
        this.baseUrl = url;
        return (T) this;
    }

    public T withAuthentication(AuthenticationBean authentication)
    {
        this.authentication = authentication;
        return (T) this;
    }

    public T withLicensing(Boolean enable)
    {
        this.enableLicensing = enable;
        return (T) this;
    }
    
    private void addBeanReflectivelyByType(String fieldName, ModuleList capabilities, ModuleBean bean)
    {
        Class beanClass = bean.getClass();
        try
        {
            Field field = capabilities.getClass().getDeclaredField(fieldName);
            Type fieldType = field.getGenericType();

            if (fieldType.equals(beanClass))
            {
                field.setAccessible(true);
                field.set(capabilities, bean);
            }
            else if (isParameterizedList(fieldType))
            {
                Type listType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (listType.equals(beanClass))
                {
                    field.setAccessible(true);
                    List beanList = (List) field.get(capabilities);
                    beanList.add(bean);
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to access module field for bean of type: " + bean.getClass(),e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Unable to find module field '" + fieldName + "' for bean of type: " + bean.getClass());
        }
    }

    public AuthenticationBean getAuthentication()
    {
        return authentication;
    }

    public LifecycleBean getLifecycle()
    {
        return lifecycle;
    }

    public B build()
    {
        return (B) new ConnectAddonBean(this);
    }
}
