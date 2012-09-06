package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.xmlrpc.ServiceBean;
import com.atlassian.xmlrpc.ServiceBeanField;

import java.util.Date;

/**
 *
 */
@ServiceBean
public class Page
{
    private String id;
    private String spaceKey;
    private String parentId;
    private String title;
    private String url;
    private int version;
    private String content;
    private Date created;
    private String creatorName;
    private Date modified;
    private String modifierName;
    private boolean homePage;
    private int locks;
    private String contentStatus;
    private boolean current;

    public String getId()
    {
        return id;
    }

    @ServiceBeanField("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public String getSpaceKey()
    {
        return spaceKey;
    }

    @ServiceBeanField("space")
    public void setSpaceKey(String spaceKey)
    {
        this.spaceKey = spaceKey;
    }

    public String getParentId()
    {
        return parentId;
    }

    @ServiceBeanField("parent")
    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public String getTitle()
    {
        return title;
    }

    @ServiceBeanField("title")
    public void setTitle(String title)
    {
        this.title = title;
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

    public int getVersion()
    {
        return version;
    }

    @ServiceBeanField("version")
    public void setVersion(int version)
    {
        this.version = version;
    }

    public String getContent()
    {
        return content;
    }

    @ServiceBeanField("content")
    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getCreated()
    {
        return created;
    }

    @ServiceBeanField("created")
    public void setCreated(Date created)
    {
        this.created = created;
    }

    public String getCreatorName()
    {
        return creatorName;
    }

    @ServiceBeanField("creatorName")
    public void setCreatorName(String creatorName)
    {
        this.creatorName = creatorName;
    }

    public Date getModified()
    {
        return modified;
    }

    @ServiceBeanField("modified")
    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    public String getModifierName()
    {
        return modifierName;
    }

    @ServiceBeanField("modifier")
    public void setModifierName(String modifierName)
    {
        this.modifierName = modifierName;
    }

    public boolean isHomePage()
    {
        return homePage;
    }

    @ServiceBeanField("homePage")
    public void setHomePage(boolean homePage)
    {
        this.homePage = homePage;
    }

    public int getLocks()
    {
        return locks;
    }

    @ServiceBeanField("locks")
    public void setLocks(int locks)
    {
        this.locks = locks;
    }

    public String getContentStatus()
    {
        return contentStatus;
    }

    @ServiceBeanField("contentStatus")
    public void setContentStatus(String contentStatus)
    {
        this.contentStatus = contentStatus;
    }

    public boolean isCurrent()
    {
        return current;
    }

    @ServiceBeanField("current")
    public void setCurrent(boolean current)
    {
        this.current = current;
    }
}
