package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.SearchRequestViewModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.nested.I18nProperty.empty;

/**
 * A Search Request View allows you to render a custom representation of a search result. Rendering a custom XML format
 * is a common example.
 *
 * After an add-on declaring a Search Request View module is installed, a new entry will show up in the
 * *Export* menu on the Issue Navigator page. Clicking the entry will redirect to the URL that is provided
 * by your add-on, passing in the issue keys, pagination information and the signed parameters that allow you
 * to verify the validity of the request.
 *
 * To declare a Search Request View, you must mainly provide the URL that will handle the HTTP GET request.
 * This URL is relative to the base url of the descriptor.
 *
 * Your service will be invoked with these parameters:
 *
 * * `issues`: A comma-separated list of issue keys
 * * `link`: A link back to the JIRA Issue Navigator where the action was invoked
 * * `startIssue`: The index of the first passed issue key in the list of all issues
 * * `endIssue`: The index of the last passed issue key in the list of all issues
 * * `totalIssues`: The number of issues in the entire search result
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#SEARCH_VIEW_EXAMPLE}
 * @schemaTitle Search Request View
 * @since 1.0
 */
@SchemaDefinition("searchRequestView")
public class SearchRequestViewModuleBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The URL of the service that will render the representation for the result set. The URL is
     * interpreted relative to the *baseUrl* in the descriptor.
     */
    @Required
    private String url;

    /**
     * Determines the order in which the Search Request View entry appears in the *Export* menu.
     *
     * The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the menu or list.
     */
    private Integer weight;

    /**
     * A description of your Search Request View
     */
    private I18nProperty description;

    public SearchRequestViewModuleBean()
    {
        this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
        this.url = "";
        this.description = empty();
    }

    public SearchRequestViewModuleBean(SearchRequestViewModuleBeanBuilder builder)
    {
        super(builder);

        if (null == weight)
        {
            this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
        }
        if (null == url)
        {
            url = "";
        }
        if (null == description)
        {
            description = empty();
        }
    }

    public static SearchRequestViewModuleBeanBuilder newSearchRequestViewModuleBean()
    {
        return new SearchRequestViewModuleBeanBuilder();
    }

    public static SearchRequestViewModuleBeanBuilder newSearchRequestViewModuleBean(SearchRequestViewModuleBean defaultBean)
    {
        return new SearchRequestViewModuleBeanBuilder(defaultBean);
    }

    public Integer getWeight()
    {
        return weight;
    }

    public String getUrl()
    {
        return url;
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public URI createUri() throws URISyntaxException
    {
        return null == url ? null : new URI(url);
    }
}
