package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.AutoconvertBeanBuilder;

public class AutoconvertBean extends BaseModuleBean {

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

    public String getPattern() {
        return pattern;
    }
}
