package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;

public class MatcherBeanBuilder {

    private String pattern;

    public MatcherBeanBuilder() {
    }

    public MatcherBeanBuilder(MatcherBean defaultBean) {
        this.pattern = defaultBean.getPattern();
    }

    public MatcherBeanBuilder withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public MatcherBean build() {
        return new MatcherBean(this);
    }
}
