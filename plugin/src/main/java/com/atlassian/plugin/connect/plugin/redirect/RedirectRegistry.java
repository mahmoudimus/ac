package com.atlassian.plugin.connect.plugin.redirect;

public interface RedirectRegistry
{
    void register(String addonKey, String moduleKey, RedirectData renderStrategy);

    RedirectData get(String addonKey, String moduleKey);
}



