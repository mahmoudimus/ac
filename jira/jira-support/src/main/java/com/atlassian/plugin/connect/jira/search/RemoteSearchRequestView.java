package com.atlassian.plugin.connect.jira.search;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.SingleIssueWriter;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import static java.util.Collections.emptyMap;

/**
 * A remote search request review that will do an html redirect to the remote plugin
 */
public class RemoteSearchRequestView implements SearchRequestView {
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final TemplateRenderer templateRenderer;
    private final ConnectUriFactory connectUriFactory;

    private final String pluginKey;
    private final String moduleKey;
    private final URI createUri;
    private final String displayName;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public RemoteSearchRequestView(
            ApplicationProperties applicationProperties,
            SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            TemplateRenderer templateRenderer,
            ConnectUriFactory connectUriFactory,
            String pluginKey,
            String moduleKey,
            URI createUri,
            String displayName,
            JiraAuthenticationContext jiraAuthenticationContext) {
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.templateRenderer = templateRenderer;
        this.connectUriFactory = connectUriFactory;
        this.createUri = createUri;
        this.displayName = displayName;
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public void init(SearchRequestViewModuleDescriptor moduleDescriptor) {
    }

    @Override
    public void writeHeaders(SearchRequest searchRequest, RequestHeaders requestHeaders, SearchRequestParams searchRequestParams) {
    }

    @Override
    public void writeSearchResults(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams, final Writer writer) {
        String baseUrl = applicationProperties.getBaseUrl(UrlMode.CANONICAL);

        String link = SearchRequestViewUtils.getLink(searchRequest, baseUrl, jiraAuthenticationContext.getUser());
        int startIssue = searchRequestParams.getPagerFilter().getStart();
        long totalIssues = getSearchCount(searchRequest, searchRequestParams);
        long tempMax = searchRequestParams.getPagerFilter().getMax() < 0 ? 0 : searchRequestParams.getPagerFilter().getMax();
        String endIssues = String.valueOf(Math.min(startIssue + tempMax, totalIssues));
        String issueKeysValue = getIssueKeysList(searchRequest, searchRequestParams);

        String signedAddonURL = connectUriFactory.createConnectAddonUriBuilder()
                .addon(pluginKey)
                .namespace(moduleKey)
                .urlTemplate(createUri.toString())
                .context(new HashMapModuleContextParameters(emptyMap()))
                .param("link", link)
                .param("startIssue", String.valueOf(startIssue))
                .param("endIssue", endIssues)
                .param("totalIssues", String.valueOf(totalIssues))
                .param("issues", issueKeysValue)
                .build();

        try {
            templateRenderer.render("velocity/view-search-request-redirect.vm", ImmutableMap.<String,
                    Object>of(
                    "redirectUrl", signedAddonURL,
                    "name", displayName

            ), writer);
        } catch (IOException e) {
            throw new DataAccessException(e);
        }
    }

    private String getIssueKeysList(SearchRequest searchRequest,
                                    SearchRequestParams searchRequestParams) {
        StringWriter issueKeys = new StringWriter();
        final SingleIssueWriter singleIssueWriter = (issue, issueView, writer) -> {
            writer.write(issue.getKey());
            writer.write(",");
        };

        try {
            searchRequestViewBodyWriterUtil.writeBody(issueKeys, null, searchRequest, singleIssueWriter,
                    searchRequestParams.getPagerFilter());
        } catch (IOException | SearchException e1) {
            throw new DataAccessException(e1);
        }

        String issueKeysValue = issueKeys.toString();
        if (!issueKeysValue.isEmpty()) {
            issueKeysValue = issueKeysValue.substring(0, issueKeysValue.length() - 1);
        }
        return issueKeysValue;
    }

    /*
     * Get the total search count. The search count would first be retrieved from the SearchRequestParams. If not found,
     * retrieve using the search provider instead.
     */
    private long getSearchCount(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams) {
        final String searchCount = (String) searchRequestParams.getSession().get("searchCount");
        if (StringUtils.isNumeric(searchCount)) {
            return Long.parseLong(searchCount);
        } else {
            try {
                return searchRequestViewBodyWriterUtil.searchCount(searchRequest);
            } catch (final SearchException se) {
                return 0;
            }
        }
    }
}
