package com.atlassian.plugin.connect.jira.search;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.atlassian.jira.search.SearchResultItem;
import com.atlassian.jira.search.WebSearcher;
import com.atlassian.jira.user.ApplicationUser;

public final class ConnectWebSearcher implements WebSearcher
{
    private final String categoryName;
    private final URI url;
    private final String key;

    public ConnectWebSearcher(final String categoryName, final URI url, final String key)
    {
        this.categoryName = categoryName;
        this.url = url;
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String getCategoryName()
    {
        return categoryName;
    }

    @Override
    public Optional<URI> getCustomUrl()
    {
        return Optional.of(url);
    }

    @Override
    public List<SearchResultItem> search(final ApplicationUser user, final String query)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<SearchResultItem> getRecentItems(final ApplicationUser user)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
