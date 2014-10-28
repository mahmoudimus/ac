package com.atlassian.plugin.connect.testsupport.filter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultAddonTestFilterResults implements AddonTestFilterResults
{
    private volatile ConcurrentHashMap<String, BlockingQueue<ServletRequestSnapshot>> requestMap;

    public DefaultAddonTestFilterResults()
    {
        this.requestMap = new ConcurrentHashMap<String, BlockingQueue<ServletRequestSnapshot>>();
    }

    @Override
    public void put(String key, ServletRequestSnapshot req)
    {
        BlockingQueue<ServletRequestSnapshot> existingQueue, newQueue;
        newQueue = new LinkedBlockingQueue<ServletRequestSnapshot>();
        existingQueue = requestMap.putIfAbsent(key, newQueue);
        try
        {
            if (existingQueue == null)
            {
                newQueue.put(req);
            }
            else
            {
                existingQueue.put(req);
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServletRequestSnapshot getRequest(String addonKey, String resource)
    {
        String res = (resource.startsWith("/")) ? resource : "/" + resource;
        requestMap.putIfAbsent(addonKey + res, new LinkedBlockingQueue<ServletRequestSnapshot>());
        try
        {
            return requestMap.get(addonKey + res).poll(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearRequest(String addonKey, String resource)
    {
        String res = (resource.startsWith("/")) ? resource : "/" + resource;
        BlockingQueue<ServletRequestSnapshot> queue = requestMap.get(addonKey + res);
        if (queue != null)
        {
            queue.clear();
        }
    }
}
