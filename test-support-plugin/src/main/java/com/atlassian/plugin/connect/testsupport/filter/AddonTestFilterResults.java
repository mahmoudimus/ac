package com.atlassian.plugin.connect.testsupport.filter;

public interface AddonTestFilterResults
{

    void put(String key, ServletRequestSnapshot req);

    ServletRequestSnapshot getRequest(String addonKey, String resource);

    void clearRequest(String addonKey, String resource);
}
