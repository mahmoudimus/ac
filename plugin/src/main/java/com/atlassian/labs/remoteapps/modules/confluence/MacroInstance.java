package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.renderer.v2.macro.Macro;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class MacroInstance
{
    final ConversionContext conversionContext;
    final String path;
    final ApplicationLinkOperationsFactory.LinkOperations linkOperations;
    final String body;
    final Map<String,String> parameters;

    public MacroInstance(ConversionContext conversionContext, String path, String body, Map<String, String> parameters, ApplicationLinkOperationsFactory.LinkOperations linkOperations)
    {
        this.conversionContext = conversionContext;
        this.path = path;
        this.body = body;
        this.parameters = parameters;
        this.linkOperations = linkOperations;
    }

    public ConversionContext getConversionContext()
    {
        return conversionContext;
    }

    public ContentEntityObject getEntity()
    {
        return conversionContext.getEntity();
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
    
    public Map<String, String> getUrlParameters()
    {
        Map<String,String> params = newHashMap();
        params.put("cx_output_type", conversionContext.getOutputType());
        if (conversionContext.getEntity() != null)
        {
            String pageId = conversionContext.getEntity().getIdAsString();
            String pageTitle = conversionContext.getEntity().getTitle();
            params.put("cx_page_id", pageId);
            params.put("cx_page_title", pageTitle);

            // deprecated
            params.put("page_id", pageId);
            params.put("pageId", pageId);
            params.put("pageTitle", pageTitle);
        }

        params.put("body", body);
        params.put("key", getHashKey());

        params.putAll(getParameters());
        params.remove(Macro.RAW_PARAMS_KEY);
        return params;
    }

    public ApplicationLinkOperationsFactory.LinkOperations getLinkOperations()
    {
        return linkOperations;
    }

    public String getHashKey()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(linkOperations.get().getId().get()).append("|");
        sb.append(parameters.toString()).append("|");
        sb.append(body).append("|");
        sb.append(path).append("|");
        sb.append(conversionContext.getEntity().getIdAsString());
        return String.valueOf(sb.toString().hashCode());
    }
}
