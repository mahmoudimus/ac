package com.atlassian.plugin.connect.modules.beans.builder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Collections2.transform;

/**
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class ConnectAddonBeanBuilder<T extends ConnectAddonBeanBuilder,
        M extends ModuleList, B extends ConnectAddonBean<M>> extends BaseModuleBeanBuilder<T, B>
{
    private String key;
    private String name;
    private String version;
    private Integer apiVersion;
    private String description;
    private VendorBean vendor;
    private Map<String, String> links;
    private M modules;
    // TODO: temp workaround till we remove use of ConnectAddonBeanBuilder.withModules
    private ModuleListBuilder<?, M> moduleListBuilder;
    private Set<ScopeName> scopes;
    private LifecycleBean lifecycle;
    private String baseUrl;
    private AuthenticationBean authentication;
    private Boolean enableLicensing;

    public ConnectAddonBeanBuilder()
    {
    }

    public ConnectAddonBeanBuilder(ConnectAddonBean<M> defaultBean)
    {
        this.key = defaultBean.getKey();
        this.name = defaultBean.getName();
        this.version = defaultBean.getVersion();
        this.apiVersion = defaultBean.getApiVersion();
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

    public T withModuleList(M modules)
    {
        this.modules = modules;
        return (T) this;
    }

    @Deprecated // use ModuleListBuilder
    public T withModules(String fieldName, ModuleBean... beans)
    {
        if (null == moduleListBuilder)
        {
            // TODO: hardwiring in the first phase of the ModuleList refactor
            this.moduleListBuilder = (ModuleListBuilder<?, M>) new JiraConfluenceModuleListBuilder();
        }

        moduleListBuilder.withModules(fieldName, beans);
        return (T) this;
    }

    @Deprecated // use ModuleListBuilder
    public T withModule(String fieldName, ModuleBean bean)
    {
        if (null == moduleListBuilder)
        {
            // TODO: hardwiring in the first phase of the ModuleList refactor
            this.moduleListBuilder = (ModuleListBuilder<?, M>) new JiraConfluenceModuleListBuilder();
        }

        moduleListBuilder.withModule(fieldName, bean);
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
        if (moduleListBuilder != null)
        {
            modules = moduleListBuilder.build();
        }
        return (B) new ConnectAddonBean(this);
    }
}
