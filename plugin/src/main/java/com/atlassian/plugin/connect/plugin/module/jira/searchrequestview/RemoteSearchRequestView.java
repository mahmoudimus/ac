package com.atlassian.plugin.connect.plugin.module.jira.searchrequestview;

import com.atlassian.jira.ComponentManager;
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
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * A remote search request review that will do an html redirect to the remote plugin
 */
public class RemoteSearchRequestView implements SearchRequestView
{
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final TemplateRenderer templateRenderer;
    private final String appKey;
    private final URI path;
    private final String name;
    private final RemotablePluginAccessor remotablePluginAccessor;

    public RemoteSearchRequestView(
            ApplicationProperties applicationProperties,
            final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil,
            TemplateRenderer templateRenderer, String appKey, URI path, String name,
            RemotablePluginAccessor remotablePluginAccessor)
    {
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.templateRenderer = templateRenderer;
        this.appKey = appKey;
        this.path = path;
        this.name = name;
        this.remotablePluginAccessor = checkNotNull(remotablePluginAccessor);
    }

    @Override
    public void init(SearchRequestViewModuleDescriptor moduleDescriptor)
    {
    }

    @Override
    public void writeHeaders(SearchRequest searchRequest, RequestHeaders requestHeaders,
            SearchRequestParams searchRequestParams)
    {
    }

    @Override
    public void writeSearchResults(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams, final Writer writer)
    {
        JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        Map<String,String> queryParams = newHashMap();
        String baseUrl = applicationProperties.getBaseUrl();
        queryParams.put("link", SearchRequestViewUtils.getLink(searchRequest,
                baseUrl, jiraAuthenticationContext.getLoggedInUser()));

        int startIssue = searchRequestParams.getPagerFilter().getStart();
        queryParams.put("startIssue", String.valueOf(startIssue));

        long totalIssues = getSearchCount(searchRequest, searchRequestParams);
        queryParams.put("totalIssues", String.valueOf(totalIssues));

        final long tempMax = searchRequestParams.getPagerFilter().getMax() < 0 ? 0 : searchRequestParams.getPagerFilter().getMax();
        queryParams.put("endIssue", String.valueOf(Math.min(startIssue + tempMax, totalIssues)));

        String issueKeysValue = getIssueKeysList(searchRequest, searchRequestParams);
        queryParams.put("issues", issueKeysValue);

        Uri target = Uri.fromJavaUri(path);
        UriBuilder b = new UriBuilder(target);
        b.addQueryParameters(queryParams);

        String signedAddonURL = remotablePluginAccessor.signGetUrl(b.toUri().toJavaUri(), ImmutableMap.<String, String[]>of());

        try
        {
            templateRenderer.render("velocity/view-search-request-redirect.vm", ImmutableMap.<String,
                    Object>of(
                    "redirectUrl", signedAddonURL,
                    "name", name

            ), writer);
        }
        catch (IOException e)
        {
            throw new DataAccessException(e);
        }
    }

    private String getIssueKeysList(SearchRequest searchRequest,
            SearchRequestParams searchRequestParams)
    {
        StringWriter issueKeys = new StringWriter();
        final SingleIssueWriter singleIssueWriter = new SingleIssueWriter()
        {
            public void writeIssue(final Issue issue, final AbstractIssueView issueView, final Writer writer)
                    throws IOException
            {
                writer.write(issue.getKey());
                writer.write(",");
            }
        };

        try
        {
            searchRequestViewBodyWriterUtil.writeBody(issueKeys, null, searchRequest, singleIssueWriter,
                    searchRequestParams.getPagerFilter());
        }
        catch (IOException e1)
        {
            throw new DataAccessException(e1);
        }
        catch (SearchException e1)
        {
            throw new DataAccessException(e1);
        }

        String issueKeysValue = issueKeys.toString();
        issueKeysValue = issueKeysValue.substring(0, issueKeysValue.length() - 1);
        return issueKeysValue;
    }

    /*
     * Get the total search count. The search count would first be retrieved from the SearchRequestParams. If not found,
     * retrieve using the search provider instead.
     */
    private long getSearchCount(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams)
    {
        final String searchCount = (String) searchRequestParams.getSession().get("searchCount");
        if (StringUtils.isNumeric(searchCount))
        {
            return Long.parseLong(searchCount);
        }
        else
        {
            try
            {
                return searchRequestViewBodyWriterUtil.searchCount(searchRequest);
            }
            catch (final SearchException se)
            {
                return 0;
            }
        }
    }
}
