package it.com.atlassian.plugin.connect;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class AddonTestFilterResults
{
    private Map<String, HttpServletRequest> requestMap;

    public AddonTestFilterResults()
    {
        this.requestMap = new HashMap<String, HttpServletRequest>();
    }
    
    public void put(String key, HttpServletRequest req)
    {
        requestMap.put(key,req);
    }

    public HttpServletRequest getRequest(String addonKey, String resource)
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
