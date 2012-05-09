package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
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
        Map<String,String> params = newHashMap();

        /*!
        ### Context Parmeters

        Query parameters that provide information about the context in which the macro is being
        rendered are prefixed with `ctx_`.

        * `ctx_output_type` - The output type for the rendering that is executing the macro.  Possible
            values include:<ul>
            <li>`preview`</li>
            <li>`display`</li>
            <li>`word` (Microsoft Word export)</li>
            <li>`pdf` (PDF export)</li>
            <li>`html_export`</li>
            </ul>
        * `ctx_page_id` - The page or blog post id of the containing entity
        * `ctx_page_title` - The page or blog post title as a convenience.  Any further information
            on the entity must be looked up via a separate RPC call/REST resource retrieval.
        */
        params.put("ctx_output_type", conversionContext.getOutputType());
        if (conversionContext.getEntity() != null)
        {
            String pageId = conversionContext.getEntity().getIdAsString();
            String pageTitle = conversionContext.getEntity().getTitle();
            params.put("ctx_page_id", pageId);
            params.put("ctx_page_type", conversionContext.getEntity().getType());
            params.put("ctx_page_title", pageTitle);

            /*!
            #### Deprecated

            The following parameters are included, but are depecated and will be removed before 1.0:

            * `page_id`
            * `pageId`
            * `pageTitle`
             */
            params.put("page_id", pageId);
            params.put("pageId", pageId);
            params.put("pageTitle", pageTitle);
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
        params.putAll(getParameters());
        params.remove(Macro.RAW_PARAMS_KEY);
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
        StringBuilder sb = new StringBuilder();
        sb.append(linkOperations.get().getId().get()).append("|");
        sb.append(parameters.toString()).append("|");
        sb.append(body).append("|");
        sb.append(path).append("|");
        sb.append(conversionContext.getEntity().getIdAsString());
        return String.valueOf(sb.toString().hashCode());
    }
}
