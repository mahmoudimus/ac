package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.xmlrpc.ServiceBean;
import com.atlassian.xmlrpc.ServiceBeanField;

/**
 *
 */
@ServiceBean
public class Space
{
    private String key;
    private String name;
    private String url;
    private String homePage;
    private String description;

    public String getName()
    {
        return name;
    }

    @ServiceBeanField("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    @ServiceBeanField("key")
    public void setKey(String key)
    {
        this.key = key;
    }

    public String getUrl()
    {
        return url;
    }

    @ServiceBeanField("url")
    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    @ServiceBeanField("description")
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getHomePage()
    {
        return homePage;
    }

    @ServiceBeanField("homePage")
    public void setHomePage(String homePage)
    {
        this.homePage = homePage;
    }
}
