package com.atlassian.plugin.connect.test.filter;

public interface AddonTestFilterResults
{

    void put(String key, ServletRequestSnaphot req);

    ServletRequestSnaphot getRequest(String addonKey, String resource);

    void clearRequest(String addonKey, String resource);
}
