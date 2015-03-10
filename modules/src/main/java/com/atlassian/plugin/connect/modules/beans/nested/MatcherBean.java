package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MatcherBeanBuilder;

@SchemaDefinition("matchers")
public class MatcherBean extends BaseModuleBean {

    @Required
    private String pattern;

    public MatcherBean() {
        this.pattern = "";
    }

    public MatcherBean(MatcherBeanBuilder builder) {
        super(builder);

        if (null == pattern)
        {
            this.pattern = "";
        }
    }

    public static MatcherBeanBuilder newMatcherBean()
    {
        return new MatcherBeanBuilder();
    }

    public static MatcherBeanBuilder newMatcherBean(MatcherBean defaultBean)
    {
        return new MatcherBeanBuilder(defaultBean);
    }

    public String getPattern() {
        return pattern;
    }
}
