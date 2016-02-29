package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.VendorBean;

/**
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class VendorBeanBuilder {

    private String name;
    private String url;

    public VendorBeanBuilder() {
    }

    public VendorBeanBuilder(VendorBean defaultBean) {
        this.name = defaultBean.getName();
        this.url = defaultBean.getUrl();
    }

    public VendorBeanBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VendorBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public VendorBean build() {
        return new VendorBean(this);
    }
}
