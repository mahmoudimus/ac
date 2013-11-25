package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SearchRequestViewCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty.empty;

/**
 * A Search Request View allows you to render a custom representation of a search result. Rendering a custom XML format
 * is a common example.
 *
 * After an add-on declaring a Search Request View capability is installed, a new entry will show up in the
 * *Export* menu on the Issue Navigator page. Clicking the entry will redirect to the URL that is provided
 * by your add-on, passing in the issue keys, pagination information and the OAuth parameters that allow you
 * to verify the validity of the request.
 *
 * To declare a Search Request View, you must mainly provide the URL that will handle the HTTP GET request.
 * This URL is relative to the base url of the descriptor. Here is an example:
 *
 *     "jiraSearchRequestViews": [
 *        {
 *           "conditions": [
 *              {
 *                 "condition": "user_is_logged_in",
 *                 "invert": false
 *              }
 *           ],
 *           "description": {
 *              "i18n": "my.searchRequestView.desc",
 *              "value": "My description"
 *           },
 *           "name": {
 *              "i18n": "my.searchRequestView.name",
 *              "value": "My Name"
 *           },
 *           "url": "/search-request.csv",
 *           "weight": 10
 *        }
 *     ]
 *
 * Your service will be invoked with these parameters:
 *
 * * __issues__: A comma-separated list of issue keys
 * * __link__: A link back to the JIRA Issue Navigator where the action was invoked
 * * __startIssue__: The index of the first passed issue key in the list of all issues
 * * __endIssue__: The index of the last passed issue key in the list of all issues
 * * __totalIssues__: The number of issues in the entire search result
 */
public class SearchRequestViewCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    /**
     * The URL of the service that will render the representation for the result set. The URL is
     * interpreted relative to the *baseUrl* in the descriptor.
     */
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

    public SearchRequestViewCapabilityBean()
    {
        this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
        this.url = "";
        this.description = empty();
    }

    public SearchRequestViewCapabilityBean(SearchRequestViewCapabilityBeanBuilder builder)
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

    public static SearchRequestViewCapabilityBeanBuilder newSearchRequestViewCapabilityBean()
    {
        return new SearchRequestViewCapabilityBeanBuilder();
    }

    public static SearchRequestViewCapabilityBeanBuilder newSearchRequestViewCapabilityBean(SearchRequestViewCapabilityBean defaultBean)
    {
        return new SearchRequestViewCapabilityBeanBuilder(defaultBean);
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
