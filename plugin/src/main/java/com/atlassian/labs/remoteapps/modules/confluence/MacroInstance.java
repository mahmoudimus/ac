package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.labs.remoteapps.util.contextparameter.RequestContextParameters;
import com.atlassian.renderer.v2.macro.Macro;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/*!
  Each instance of a Remote App macro, no matter if the macro is rendered from retrieved storage-format
  XML or from an IFrame, shares certain properties.
 */

/*!-constructor and getters */
public class MacroInstance
{
    final ConversionContext conversionContext;
    final String path;
    private final RequestContextParameters requestContextParameters;
    final ApplicationLinkOperationsFactory.LinkOperations linkOperations;
    final String body;
    final Map<String,String> parameters;
    final Map<String, String> allContextParameters;

    public MacroInstance(ConversionContext conversionContext, String path, String body,
            Map<String, String> parameters, RequestContextParameterFactory requestContextParameterFactory,
            ApplicationLinkOperationsFactory.LinkOperations linkOperations)
    {
        this.conversionContext = conversionContext;
        this.path = path;
        this.body = body;
        this.parameters = parameters;
        this.allContextParameters = getAllContextParameters();
        this.requestContextParameters = requestContextParameterFactory.create(getAllContextParameters());
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

    public ApplicationLinkOperationsFactory.LinkOperations getLinkOperations()
    {
        return linkOperations;
    }

    /*!#url-parameters

    ## URL Parameters

    Every macro instance will have its content retrieved from the Remote App on either the server
    or client side.  As part of this URL construction, key query parameters are added to the URL
    to pass along macro instance information as well as give the context in which the macro was
    rendered.
    */
    public Map<String, String> getUrlParameters()
    {
        Map<String,String> params = newHashMap(requestContextParameters.getQueryParameters());

        /*!
        #### Deprecated

        The following parameters are included, but are deprecated and will be removed before 1.0:

        * `page_id`
        * `pageId`
        * `pageTitle`
         */
        if (requestContextParameters.isLegacyMode())
        {
            params.put("page_id", allContextParameters.get("page_id"));
            params.put("pageId", allContextParameters.get("page_id"));
            params.put("pageTitle", allContextParameters.get("page_title"));
        }

        /*!
        ### Macro Instance Parameters

        * `body` - The body of the macro, if allowed. Empty string if not present.
        * `key` - The <a href="#hash">unique hash key</a> of the macro instance.
         */
        params.put("body", body);
        params.put("key", getHashKey());

        /*!
        ### Macro Parameters

        Finally, any configured macro parameters and their values are sent as query parameters who's
        name exactly matches the configured parameter name.
         */
        params.putAll(parameters);
        params.remove(Macro.RAW_PARAMS_KEY);
        return params;
    }

    private Map<String,String> getAllContextParameters()
    {
        Map<String,String> params = newHashMap();

        params.put("output_type", conversionContext.getOutputType());

        if (conversionContext.getEntity() != null)
        {
            String pageId = conversionContext.getEntity().getIdAsString();
            String pageTitle = conversionContext.getEntity().getTitle();
            pageTitle = pageTitle != null ? pageTitle : "";
            params.put("page_id", pageId);
            params.put("page_type", conversionContext.getEntity().getType());
            params.put("page_title", pageTitle);
        }
        else
        {
            params.put("page_id", "");
            params.put("page_title", "");
            params.put("page_type", "");
        }
        return params;
    }

    /*!#hash

    ## Unique Hash Key

    Every macro instance has a unique hash key that represents the state of key values in the macro
    instance.  The primary usage of this key value is to be the cache key for retrieved storage format
    macro types, though it may be generally useful for other types.

    The hash is composed of the following properties:

    * The app base URL
    * The macro parameters' values
    * The macro body
    * The relative path to the app macro retrieval/IFrame URL
    * The entity (usually page) id

    Any values outside these listed, including things like the page title, are not included in the
     key.  If an app needs to know when they change, the app must register a web hook and update
     that information itself.
     */
    public String getHashKey()
    {
        String entityId = conversionContext.getEntity() != null ? conversionContext.getEntity().getIdAsString() :
                "";
        StringBuilder sb = new StringBuilder();
        sb.append(linkOperations.get().getId().get()).append("|");
        sb.append(parameters.toString()).append("|");
        sb.append(body).append("|");
        sb.append(path).append("|");
        sb.append(entityId);
        return String.valueOf(sb.toString().hashCode());
    }

    public Map<String, String> getHeaders()
    {
        return requestContextParameters.getHeaders();
    }
}
