package com.atlassian.plugin.connect.testsupport.filter;

import java.util.HashMap;
import java.util.Map;

public class DefaultAddonTestFilterResults implements AddonTestFilterResults
{
    private Map<String, ServletRequestSnapshot> requestMap;

    public DefaultAddonTestFilterResults()
    {
        this.requestMap = new HashMap<String, ServletRequestSnapshot>();
    }

    @Override
    public void put(String key, ServletRequestSnapshot req)
    {
        requestMap.put(key, req);
    }

    @Override
    public ServletRequestSnapshot getRequest(String addonKey, String resource)
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
