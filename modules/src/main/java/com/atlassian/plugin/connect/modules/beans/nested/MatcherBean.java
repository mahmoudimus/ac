package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MatcherBeanBuilder;

/**
 *
 * Matchers define a URL string as part of an Autoconvert definition.
 *
 * Wildcards (parts of the url that should always match, such as as a unique ID) can be defined using a single open and close
 * curly bracket such as '{}'. Use a new brace pair for each separate wildcard.
 *
 * Keep in mind that to match the same link string but using different internet protocols (such as http vs. https), you will need to
 * explicitly define a new matcher for each one.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#AUTOCONVERT_MATCHER_EXAMPLE}
 * @schemaTitle Matcher
 * @since 1.1
 */
@SchemaDefinition("matchers")
public class MatcherBean extends BaseModuleBean {

    /**
     * The pattern is a string that defines a single URL to match.
     */
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
