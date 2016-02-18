package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;

/**
 *
 */
public class UiOverrideBean {
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    @Required
    private String type; /*TODO: work out how to use the LayoutType enum here?*/

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }
}
