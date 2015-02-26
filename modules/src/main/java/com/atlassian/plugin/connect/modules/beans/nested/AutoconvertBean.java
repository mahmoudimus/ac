package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.AutoconvertBeanBuilder;

@SchemaDefinition("autoconvert")
public class AutoconvertBean extends BaseModuleBean {

    @Required
    private String pattern;

    public AutoconvertBean() {
        this.pattern = "";
    }

    public AutoconvertBean(AutoconvertBeanBuilder builder) {
        super(builder);

        if (null == pattern)
        {
            this.pattern = "";
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

    public String getPattern() {
        return pattern;
    }
}
