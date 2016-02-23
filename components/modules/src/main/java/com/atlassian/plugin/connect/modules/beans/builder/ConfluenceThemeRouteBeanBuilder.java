package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteBean;

/**
 *
 */
public class ConfluenceThemeRouteBeanBuilder<BUILDER extends ConfluenceThemeRouteBeanBuilder, BEAN extends ConfluenceThemeRouteBean> {
    private String url;

    public ConfluenceThemeRouteBeanBuilder() {
    }

    public BUILDER withUrl(String url) {
        this.url = url;
        return (BUILDER) this;
    }

    public BEAN build() {
        return (BEAN) new ConfluenceThemeRouteBean(this);
    }
}
