package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.LifecycleBean;

public class LifecycleBeanBuilder extends BaseModuleBeanBuilder<LifecycleBeanBuilder, LifecycleBean> {
    private String installed;
    private String uninstalled;
    private String enabled;
    private String disabled;

    public LifecycleBeanBuilder() {
        this.installed = "";
        this.uninstalled = "";
        this.enabled = "";
        this.disabled = "";
    }

    public LifecycleBeanBuilder(LifecycleBean defaultBean) {
        this.installed = defaultBean.getInstalled();
        this.uninstalled = defaultBean.getUninstalled();
        this.enabled = defaultBean.getEnabled();
        this.disabled = defaultBean.getDisabled();
    }

    public LifecycleBeanBuilder withInstalled(String url) {
        this.installed = url;
        return this;
    }

    public LifecycleBeanBuilder withUninstalled(String url) {
        this.uninstalled = url;
        return this;
    }

    public LifecycleBeanBuilder withEnabled(String url) {
        this.enabled = url;
        return this;
    }

    public LifecycleBeanBuilder withDisabled(String url) {
        this.disabled = url;
        return this;
    }

    @Override
    public LifecycleBean build() {
        return new LifecycleBean(this);
    }
}
