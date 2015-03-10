package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class AutoconvertBeanBuilder<T extends AutoconvertBeanBuilder, B extends AutoconvertBean> extends BaseModuleBeanBuilder<T, B> {

    private String urlParameter;
    private List<MatcherBean> matchers;

    public AutoconvertBeanBuilder()
    {
    }

    public AutoconvertBeanBuilder(AutoconvertBean defaultBean) {
        this.matchers = defaultBean.getMatchers();
        this.urlParameter = defaultBean.getUrlParameter();
    }

    public T withMatchers(MatcherBean... matchers)
    {
        this.matchers = ImmutableList.copyOf(matchers);
        return (T) this;
    }

    public T withUrlParameter(String urlParameter)
    {
        this.urlParameter = urlParameter;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new AutoconvertBean(this);
    }
}
