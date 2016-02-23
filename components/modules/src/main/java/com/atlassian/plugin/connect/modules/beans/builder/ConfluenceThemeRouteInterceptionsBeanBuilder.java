package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean;

/**
 *
 */
public class ConfluenceThemeRouteInterceptionsBeanBuilder<BUILDER extends ConfluenceThemeRouteInterceptionsBeanBuilder, BEAN extends ConfluenceThemeRouteInterceptionsBean> {
    private ConfluenceThemeRouteBean dashboard;
    private ConfluenceThemeRouteBean spaceview;
    private ConfluenceThemeRouteBean contentview;

    public ConfluenceThemeRouteInterceptionsBeanBuilder() {
    }

    public BUILDER withContentview(ConfluenceThemeRouteBean contentview) {
        this.contentview = contentview;
        return (BUILDER) this;

    }

    public BUILDER withDashboard(ConfluenceThemeRouteBean dashboard) {
        this.dashboard = dashboard;
        return (BUILDER) this;
    }

    public BUILDER withSpaceview(ConfluenceThemeRouteBean spaceview) {
        this.spaceview = spaceview;
        return (BUILDER) this;
    }

    public BEAN build() {
        return (BEAN) new ConfluenceThemeRouteInterceptionsBean(this);
    }
}
