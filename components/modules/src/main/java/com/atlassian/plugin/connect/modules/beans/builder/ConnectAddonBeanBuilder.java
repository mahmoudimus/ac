package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;

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
@SuppressWarnings("UnusedDeclaration")
public class ConnectAddonBeanBuilder {
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

    public ConnectAddonBeanBuilder() {
    }

    public ConnectAddonBeanBuilder(ShallowConnectAddonBean defaultBean) {
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

    public ConnectAddonBeanBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    public ConnectAddonBeanBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ConnectAddonBeanBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public ConnectAddonBeanBuilder withApiVersion(Integer version) {
        this.apiVersion = version;
        return this;
    }

    public ConnectAddonBeanBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ConnectAddonBeanBuilder withVendor(VendorBean vendor) {
        this.vendor = vendor;
        return this;
    }

    public ConnectAddonBeanBuilder withModules(String fieldName, final ModuleBean... beans) {
        if (null == modules) {
            this.modules = new HashMap<>();
        }

        final List<ModuleBean> totalBeans = new ArrayList<>(Arrays.asList(beans));
        if (null != modules.get(fieldName)) {
            totalBeans.addAll(modules.get(fieldName).get());
        }

        Supplier<List<ModuleBean>> moduleBeanSupplier = () -> totalBeans;

        modules.put(fieldName, moduleBeanSupplier);

        return this;
    }

    public ConnectAddonBeanBuilder withModule(String fieldName, ModuleBean bean) {
        withModules(fieldName, bean);

        return this;
    }

    public ConnectAddonBeanBuilder withModuleList(Map<String, Supplier<List<ModuleBean>>> modules) {
        this.modules = modules;

        return this;
    }

    public ConnectAddonBeanBuilder withLinks(Map<String, String> links) {
        this.links = links;

        return this;
    }

    public ConnectAddonBeanBuilder withScopes(Set<ScopeName> scopes) {
        this.scopes = ImmutableSet.copyOf(scopes);
        return this;
    }

    public ConnectAddonBeanBuilder withLifecycle(LifecycleBean lifecycle) {
        this.lifecycle = lifecycle;
        return this;
    }

    public ConnectAddonBeanBuilder withBaseurl(String url) {
        this.baseUrl = url;
        return this;
    }

    public ConnectAddonBeanBuilder withAuthentication(AuthenticationBean authentication) {
        this.authentication = authentication;
        return this;
    }

    public ConnectAddonBeanBuilder withLicensing(Boolean enable) {
        this.enableLicensing = enable;
        return this;
    }

    private static HashSet<String> transformScopeNamesToStrings(Set<ScopeName> scopeNames) {
        return new HashSet<String>(transform(scopeNames, scopeName -> null == scopeName ? null : scopeName.name()));
    }

    @SuppressWarnings("unchecked")
    private void addBeanReflectivelyByType(String fieldName, Map<String, List<JsonObject>> capabilities, ModuleBean bean) {
        Class beanClass = bean.getClass();
        try {
            Field field = capabilities.getClass().getDeclaredField(fieldName);
            Type fieldType = field.getGenericType();

            if (fieldType.equals(beanClass)) {
                field.setAccessible(true);
                field.set(capabilities, bean);
            } else if (isParameterizedList(fieldType)) {
                Type listType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (listType.equals(beanClass)) {
                    field.setAccessible(true);
                    List beanList = (List) field.get(capabilities);
                    beanList.add(bean);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access module field for bean of type: " + bean.getClass(), e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unable to find module field '" + fieldName + "' for bean of type: " + bean.getClass());
        }
    }

    public String getKey() {
        return key;
    }

    public AuthenticationBean getAuthentication() {
        return authentication;
    }

    public LifecycleBean getLifecycle() {
        return lifecycle;
    }

    public ConnectAddonBean build() {
        return new ConnectAddonBean(this);
    }
}
