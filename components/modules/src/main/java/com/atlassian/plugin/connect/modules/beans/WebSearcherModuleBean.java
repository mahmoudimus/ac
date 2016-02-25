package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class WebSearcherModuleBean extends RequiredKeyBean {

    /**
     * URL the search request will be sent to. It must contain the `{search.query}` context variable.
     *
     * `search.query` a search query as entered by the user
     */
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;

    public String getUrl() {
        return url;
    }
}
