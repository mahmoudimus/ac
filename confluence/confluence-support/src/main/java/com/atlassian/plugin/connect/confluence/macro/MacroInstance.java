package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.spaces.Spaced;
import com.atlassian.plugin.connect.api.util.RequestContextParameterFactory;
import com.atlassian.plugin.connect.api.util.RequestContextParameters;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.renderer.v2.macro.Macro;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/*!
  Each instance of a Remotable Plugin macro, no matter if the macro is rendered from retrieved storage-format
  XML or from an IFrame, shares certain properties.
 */

/*!-constructor and getters */
public class MacroInstance
{
    final ConversionContext conversionContext;
    final URI path;
    final HttpMethod method;
    final RemotablePluginAccessor remotablePluginAccessor;
    final String body;
    final Map<String,String> parameters;
    final Map<String, String> allContextParameters;
    final RequestContextParameterFactory requestContextParameterFactory;

    public MacroInstance(ConversionContext conversionContext, URI path, HttpMethod httpMethod, String body,
            Map<String, String> parameters,
            RequestContextParameterFactory requestContextParameterFactory,
            RemotablePluginAccessor remotablePluginAccessor
    )
    {
        this.conversionContext = conversionContext;
        this.path = path;
        this.method = httpMethod;
        this.body = body;
        this.parameters = parameters;
        this.remotablePluginAccessor = remotablePluginAccessor;
        this.allContextParameters = getAllContextParameters();
        this.requestContextParameterFactory = requestContextParameterFactory;
    }

    public ConversionContext getConversionContext()
    {
        return conversionContext;
    }

    public ContentEntityObject getEntity()
    {
        return conversionContext.getEntity();
    }

    public URI getPath()
    {
        return path;
    }

    public RemotablePluginAccessor getRemotablePluginAccessor()
    {
        return remotablePluginAccessor;
    }

    /*!#url-parameters

    ## URL Parameters

    Every macro instance will have its content retrieved from the Remotable Plugin on either the server
    or client side.  As part of this URL construction, key query parameters are added to the URL
    to pass along macro instance information as well as give the context in which the macro was
    rendered.
    */
    public Map<String, String> getUrlParameters(String userId, String userKey)
    {
        RequestContextParameters requestContextParameters = createRequestContextParameters(userId, userKey);
        Map<String,String> params = newHashMap(requestContextParameters.getQueryParameters());

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

        ContentEntityObject entity = conversionContext.getEntity();

        String pageId = "";
        String pageTitle = "";
        String pageType = "";
        String spaceKey = "";

        if (entity != null)
        {
            pageId = entity.getIdAsString();
            pageTitle = StringUtils.defaultString(entity.getTitle());
            pageType = entity.getType();
            if (entity instanceof Spaced) {
                spaceKey = ((Spaced) entity).getSpace().getKey();
            }
        }

        params.put("page_id", pageId);
        params.put("page_title", pageTitle);
        params.put("page_type", pageType);
        params.put("space_key", spaceKey);

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
        sb.append(remotablePluginAccessor.getKey()).append("|");
        sb.append(parameters.toString()).append("|");
        sb.append(body).append("|");
        sb.append(path).append("|");
        sb.append(method).append("|");
        sb.append(entityId);
        return String.valueOf(sb.toString().hashCode());
    }

    public Map<String, String> getHeaders(String userId, String userKey)
    {
        return createRequestContextParameters(userId, userKey).getHeaders();
    }

    private RequestContextParameters createRequestContextParameters(String userId, String userKey)
    {
        return requestContextParameterFactory.create(userId, userKey, getAllContextParameters());
    }
}
