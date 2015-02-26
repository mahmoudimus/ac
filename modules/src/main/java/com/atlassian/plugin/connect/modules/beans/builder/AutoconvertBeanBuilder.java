package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;

public class AutoconvertBeanBuilder<T extends AutoconvertBeanBuilder, B extends AutoconvertBean> extends BaseModuleBeanBuilder<T, B> {

    private String pattern;

    public AutoconvertBeanBuilder()
    {
    }

    public AutoconvertBeanBuilder(AutoconvertBean defaultBean) {
        this.pattern = defaultBean.getPattern();
    }

    public T withPattern(String pattern)
    {
        this.pattern = pattern;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new AutoconvertBean(this);
    }
}
