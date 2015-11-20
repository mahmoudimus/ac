package com.atlassian.plugin.connect.confluence.blueprint;

import java.lang.reflect.Type;
import java.util.Map;

import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.ResponseException;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A blueprint context provider that will make a remote request to the addon to generate a set of variables, used for later substitution.
 *
 * @since 1.1.60
 */
public class ConnectBlueprintContextProvider extends AbstractBlueprintContextProvider
{
    private static final Logger log = LoggerFactory.getLogger(ConnectBlueprintContextProvider.class);

    private static final Gson GSON = new Gson();
    /* this is a type token so that Gson can deserialize a map back from a simple json object*/
    private static final Type type = new TypeToken<Map<String, String>>(){}.getType();

    static final String CONTEXT_URL_KEY = "context-url";
    static final String ADDON_KEY = "addon-key";
    static final String CONTENT_TEMPLATE_KEY = "content-template-key";

    private final RequestFactory<?> requestFactory;
    private String contextUrl;
    private String addonKey;
    private String templateKey;

    @Autowired
    public ConnectBlueprintContextProvider(RequestFactory<?> requestFactory)
    {
        this.requestFactory = requestFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        super.init(params);
        Preconditions.checkArgument(params.containsKey(CONTEXT_URL_KEY), "the " + CONTEXT_URL_KEY + " is not specified. The context-provider element should not have been supplied if the connect addon blueprint module did not provide a context provider url");
        Preconditions.checkArgument(params.containsKey(ADDON_KEY), "the " + ADDON_KEY + " is not specified.");
        Preconditions.checkArgument(params.containsKey(CONTENT_TEMPLATE_KEY), "the " + CONTENT_TEMPLATE_KEY + " is not specified");
        contextUrl = params.get(CONTEXT_URL_KEY);
        addonKey = params.get(ADDON_KEY);
        templateKey = params.get(CONTENT_TEMPLATE_KEY);
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext)
    {
        //we cannot allow any exception to escape this method, because that will cause the blueprint creation to fail
        try
        {
            Request<?, ?> post = requestFactory.createRequest(Request.MethodType.POST, contextUrl);
            log.debug("executing POST to " + contextUrl);
            Body body = build(blueprintContext);
            post.setRequestBody(GSON.toJson(body));
            //TODO: design a slightly more type safe data format than Map<String, String>!
            Map<String, String> contextMap = GSON.fromJson(post.executeAndReturn(r -> (r.getResponseBodyAsString())), type);
            contextMap.forEach(blueprintContext::put);
            return blueprintContext;
        }
        catch (RuntimeException e)
        {
            log.warn("Error: "+ e.getMessage());
            if (log.isDebugEnabled())
            {
                log.debug("", e);
            }
            return blueprintContext;
        }
        catch (ResponseException e)
        {
            log.warn("Bad response from " + contextUrl + ". " + e.getMessage());
            if (log.isDebugEnabled())
            {
                log.debug("", e);
            }
            return blueprintContext;
        }
    }

    private Body build(BlueprintContext blueprintContext)
    {
        Body body = new Body();
        body.spaceKey = blueprintContext.getSpaceKey();
        body.spaceKey = addonKey;
        body.spaceKey = templateKey;
        return body;
    }

    /**
     * Pojo to hold the json format POST'ed to the addon's blueprint context url.
     */
    private class Body
    {
        private String addonKey;
        private String templateKey;
        private String spaceKey;
    }
}
