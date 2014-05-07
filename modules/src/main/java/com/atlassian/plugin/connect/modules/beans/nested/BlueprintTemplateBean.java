package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateBeanBulder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * The blueprint template defines how the content for a blueprint is loaded.
 */
public class BlueprintTemplateBean {
    private String url;
    private String content;

    public BlueprintTemplateBean(BlueprintTemplateBeanBulder builder) {
        copyFieldsByNameAndType(builder, this);
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }
}
