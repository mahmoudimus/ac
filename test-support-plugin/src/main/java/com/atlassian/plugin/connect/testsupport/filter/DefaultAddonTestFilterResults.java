package com.atlassian.plugin.connect.testsupport.filter;

import java.util.HashMap;
import java.util.Map;

public class DefaultAddonTestFilterResults implements AddonTestFilterResults
{
    private Map<String, ServletRequestSnaphot> requestMap;

    public DefaultAddonTestFilterResults()
    {
        this.requestMap = new HashMap<String, ServletRequestSnaphot>();
    }

    @Override
    public void put(String key, ServletRequestSnaphot req)
    {
        requestMap.put(key, req);
    }

    @Override
    public ServletRequestSnaphot getRequest(String addonKey, String resource)
    {
        String res = (resource.startsWith("/")) ? resource : "/" + resource;
        return requestMap.get(addonKey + res);
    }

    @Override
    public void clearRequest(String addonKey, String resource)
    {
        String res = (resource.startsWith("/")) ? resource : "/" + resource;
        requestMap.remove(addonKey + res);
    }
}
