package it.com.atlassian.plugin.connect.filter;

import java.util.HashMap;
import java.util.Map;

public class AddonTestFilterResults
{
    private Map<String, ServletRequestSnaphot> requestMap;

    public AddonTestFilterResults()
    {
        this.requestMap = new HashMap<String, ServletRequestSnaphot>();
    }

    public void put(String key, ServletRequestSnaphot req)
    {
        requestMap.put(key, req);
    }

    public ServletRequestSnaphot getRequest(String addonKey, String resource)
    {
        String res = (resource.startsWith("/")) ? resource : "/" + resource;
        return requestMap.get(addonKey + res);
    }

    public void clearRequest(String addonKey, String resource)
    {
        String res = (resource.startsWith("/")) ? resource : "/" + resource;
        requestMap.remove(addonKey + res);
    }
}
