package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.UISupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UISupportValueType;

public class UISupportBeanBuilder {

    private String defaultOperator;
    private I18nProperty name;
    private I18nProperty tooltip;
    private String dataUri;
    private UISupportValueType valueType;

    public UISupportBeanBuilder() {
    }

    public UISupportBeanBuilder(UISupportBean defaultBean) {
        this.defaultOperator = defaultBean.getDefaultOperator();
        this.dataUri = defaultBean.getDataUri();
        this.name = defaultBean.getName();
        this.tooltip = defaultBean.getTooltip();
        this.valueType = defaultBean.getValueType();
    }

    public UISupportBeanBuilder withDefaultOperator(String defaultOperator) {
        this.defaultOperator = defaultOperator;
        return this;
    }

    public UISupportBeanBuilder withName(I18nProperty name) {
        this.name = name;
        return this;
    }

    public UISupportBeanBuilder withDataUri(String dataUri) {
        this.dataUri = dataUri;
        return this;
    }

    public UISupportBeanBuilder withTooltip(I18nProperty tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public UISupportBeanBuilder withValueType(UISupportValueType valueType) {
        this.valueType = valueType;
        return this;
    }

    public UISupportBean build() {
        return new UISupportBean(this);
    }
}