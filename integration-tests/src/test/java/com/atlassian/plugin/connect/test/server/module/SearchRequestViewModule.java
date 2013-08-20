package com.atlassian.plugin.connect.test.server.module;

public final class SearchRequestViewModule extends MainModuleWithResource<SearchRequestViewModule>
{
    private SearchRequestViewModule(String key)
    {
        super("remote-search-request-view", key);
    }

    public static SearchRequestViewModule key(String key)
    {
        return new SearchRequestViewModule(key);
    }
}
