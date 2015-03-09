package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;

public class MatcherBeanBuilder<T extends MatcherBeanBuilder, B extends MatcherBean> extends BaseModuleBeanBuilder<T, B> {

    private String pattern;

    public MatcherBeanBuilder()
    {
    }

    public MatcherBeanBuilder(MatcherBean defaultBean) {
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
        return (B) new MatcherBean(this);
    }

}
