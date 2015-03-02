package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;

public class AutoconvertBeanBuilder<T extends AutoconvertBeanBuilder, B extends AutoconvertBean> extends BaseModuleBeanBuilder<T, B> {

    private String pattern;
    private String urlParameter;

    public AutoconvertBeanBuilder()
    {
    }

    public AutoconvertBeanBuilder(AutoconvertBean defaultBean) {
        this.pattern = defaultBean.getPattern();
        this.urlParameter = defaultBean.getUrlParameter();
    }

    public T withPattern(String pattern)
    {
        this.pattern = pattern;
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
