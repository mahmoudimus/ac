package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;

import java.util.Map;

/**
 *
 */
public class MacroInstance
{
    final String pageId;
    final String path;
    final ApplicationLinkOperationsFactory.LinkOperations linkOperations;
    final String body;
    final Map<String,String> parameters;

    public MacroInstance(String pageId, String path, String body, Map<String, String> parameters, ApplicationLinkOperationsFactory.LinkOperations linkOperations)
    {
        this.pageId = pageId;
        this.path = path;
        this.body = body;
        this.parameters = parameters;
        this.linkOperations = linkOperations;
    }

    public String getPageId()
    {
        return pageId;
    }


    public String getPath()
    {
        return path;
    }

    public String getBody()
    {
        return body;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public ApplicationLinkOperationsFactory.LinkOperations getLinkOperations()
    {
        return linkOperations;
    }

    public String getHashKey()
    {
        return String.valueOf(linkOperations.get().getId().get().hashCode() & parameters.hashCode() & body.hashCode() & path.hashCode() & pageId.hashCode());
    }
}
