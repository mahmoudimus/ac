package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;

public class ControlBeanBuilder extends RequiredKeyBeanBuilder<ControlBeanBuilder, ControlBean> {
    private String type;

    public ControlBeanBuilder() {

    }

    public ControlBeanBuilder(ControlBean bean) {
        super(bean);
        this.type = bean.getType();
    }

    public ControlBeanBuilder withType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public ControlBean build() {
        return new ControlBean(this);
    }
}
