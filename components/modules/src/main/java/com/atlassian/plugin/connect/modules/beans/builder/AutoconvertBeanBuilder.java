package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class AutoconvertBeanBuilder {

    private String urlParameter;
    private List<MatcherBean> matchers;

    public AutoconvertBeanBuilder() {
    }

    public AutoconvertBeanBuilder(AutoconvertBean defaultBean) {
        this.matchers = defaultBean.getMatchers();
        this.urlParameter = defaultBean.getUrlParameter();
    }

    public AutoconvertBeanBuilder withMatchers(MatcherBean... matchers) {
        this.matchers = ImmutableList.copyOf(matchers);
        return this;
    }

    public AutoconvertBeanBuilder withUrlParameter(String urlParameter) {
        this.urlParameter = urlParameter;
        return this;
    }

    public AutoconvertBean build() {
        return new AutoconvertBean(this);
    }
}
