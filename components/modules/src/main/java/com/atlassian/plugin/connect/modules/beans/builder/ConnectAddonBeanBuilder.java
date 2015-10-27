package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.isParameterizedList;
import static com.google.common.collect.Collections2.transform;

/**
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class ConnectAddonBeanBuilder<T extends ConnectAddonBeanBuilder, B extends ConnectAddonBean> extends BaseModuleBeanBuilder<T, B>
{
    private String key;
    private String name;
    private String version;
    private Integer apiVersion;
    private String description;
    private VendorBean vendor;
    private Map<String, String> links;
    private Set<ScopeName> scopes;
    private LifecycleBean lifecycle;
    private String baseUrl;
    private AuthenticationBean authentication;
    private Boolean enableLicensing;
    private Map<String, Supplier<List<ModuleBean>>> modules;

    public ConnectAddonBeanBuilder()
    {
    }

    public ConnectAddonBeanBuilder(ShallowConnectAddonBean defaultBean)
    {
        this.key = defaultBean.getKey();
        this.name = defaultBean.getName();
        this.version = defaultBean.getVersion();
        this.apiVersion = defaultBean.getApiVersion();
        this.description = defaultBean.getDescription();
        this.vendor = defaultBean.getVendor();
        this.links = defaultBean.getLinks();
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

    public T withApiVersion(Integer version)
    {
        this.apiVersion = version;
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

    public T withModules(String fieldName, final ModuleBean... beans)
    {
        if (null == modules)
        {
            this.modules = new HashMap<>();
        }

        final List<ModuleBean> totalBeans = new ArrayList(Arrays.asList(beans));
        if (null != modules.get(fieldName))
        {
            totalBeans.addAll(modules.get(fieldName).get());
        }

        Supplier<List<ModuleBean>> moduleBeanSupplier = new Supplier<List<ModuleBean>>()
        {
            @Override
            public List<ModuleBean> get()
            {
                return totalBeans;
            }
        };

        modules.put(fieldName, moduleBeanSupplier);
        
        return (T) this;
    }

    public T withModule(String fieldName, ModuleBean bean)
    {
        withModules(fieldName, bean);
        
        return (T) this;
    }
    
    public T withModuleList(Map<String, Supplier<List<ModuleBean>>> modules)
    {
        this.modules = modules;
        
        return (T) this;
    }

    public T withLinks(Map<String, String> links)
    {
        this.links = links;

        return (T) this;
    }

    public T withScopes(Set<ScopeName> scopes)
    {
        this.scopes = ImmutableSet.copyOf(scopes);
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

    private static HashSet<String> transformScopeNamesToStrings(Set<ScopeName> scopeNames)
    {
        return new HashSet<String>(transform(scopeNames, new Function<ScopeName, String>()
        {
            @Override
            public String apply(@Nullable ScopeName scopeName)
            {
                return null == scopeName ? null : scopeName.name();
            }
        }));
    }

    private void addBeanReflectivelyByType(String fieldName, Map<String, List<JsonObject>> capabilities, ModuleBean bean)
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
            throw new RuntimeException("Unable to access module field for bean of type: " + bean.getClass(), e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Unable to find module field '" + fieldName + "' for bean of type: " + bean.getClass());
        }
    }

    public String getKey() { return key; }

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