package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;

public class WebSearcherModuleBean extends RequiredKeyBean {

    /**
     * Name of the category of items this searcher returns. By convention there should be one searcher per category.
     */
    @Required
    private String categoryName;

    /**
     * The parameters are as follows:
     *
     * * `search.query` a search query as entered by the user
     */
    @Required
    @StringSchemaAttributes (format = "uri-template")
    private String url;

    public String getCategoryName() {
        return categoryName;
    }

    public String getUrl() {
        return url;
    }
}
