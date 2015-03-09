package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.AutoconvertBeanBuilder;
import com.google.common.collect.ImmutableList;

import java.util.List;

@SchemaDefinition("autoconvert")
public class AutoconvertBean extends BaseModuleBean {

    @Required
    private String urlParameter;
    private List<MatcherBean> matchers;

    public AutoconvertBean() {
        this.matchers = ImmutableList.of();
        this.urlParameter = "";
    }

    public AutoconvertBean(AutoconvertBeanBuilder builder) {
        super(builder);

        if (null == matchers)
        {
            this.matchers = ImmutableList.of();
        }

        if (null == urlParameter)
        {
            this.urlParameter = "";
        }
    }

    public static AutoconvertBeanBuilder newAutoconvertBean()
    {
        return new AutoconvertBeanBuilder();
    }

    public static AutoconvertBeanBuilder newAutoconvertBean(AutoconvertBean defaultBean)
    {
        return new AutoconvertBeanBuilder(defaultBean);
    }

    public List<MatcherBean> getMatchers() {
        return matchers;
    }

    public String getUrlParameter()
    {
        return urlParameter;
    }
}
