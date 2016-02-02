package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.confluence.api.model.content.ContentBody;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.service.content.ContentBodyConversionService;
import com.atlassian.confluence.api.service.exceptions.ServiceException;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.confluence.blueprint.event.BlueprintContextRequestFailedEvent;
import com.atlassian.plugin.connect.confluence.blueprint.event.BlueprintContextRequestSuccessEvent;
import com.atlassian.plugin.connect.confluence.blueprint.event.BlueprintContextResponseParseFailureEvent;
import com.atlassian.plugin.connect.confluence.blueprint.event.BlueprintContextResponseParseSuccessEvent;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextPostBody;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextValue;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.Promise;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.atlassian.plugin.connect.api.request.HttpMethod.POST;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.IOUtils.toInputStream;

/**
 * A blueprint context provider that will make a remote request to the addon to generate a set of variables, used for later substitution.
 *
 */
public class BlueprintContextProvider extends AbstractBlueprintContextProvider
{
    private static final Logger log = LoggerFactory.getLogger(BlueprintContextProvider.class);

    private static final Gson gson = new Gson();
    private static final Type responseType = new BlueprintContextResponseTypeToken().getType();

    // timeout in seconds for remote requests to get context values. Should be same value as set in ConnectHttpClientFactory
    private static final long MAX_TIMEOUT = 10L;
    // the name of the param holding the context url
    static final String CONTEXT_URL_KEY = "context-url";
    // the name of the param holding the key of the addon that created this module
    static final String REMOTE_ADDON_KEY = "addon-key";
    // the name of the param holding the vendor name - used for error messages
    static final String REMOTE_VENDOR_NAME = "vendor-name";
    // the name of the param holding the key of the content template for this context provider
    static final String CONTENT_TEMPLATE_KEY = "content-template-key";
    // readable name of blueprint
    static final String BLUEPRINT_NAME = "name";

    private final RemotablePluginAccessorFactory accessorFactory;
    private final ContentBodyConversionService converter;
    private final UserManager userManager;
    private final LocaleResolver localeResolver;
    private final EventPublisher eventPublisher;

    private String contextUrl;
    private String addonKey;
    private String vendorName;
    private String blueprintKey;
    private RemotablePluginAccessor pluginAccessor;
    private I18nResolver i18nResolver;

    @Autowired
    public BlueprintContextProvider(RemotablePluginAccessorFactory httpAccessor,
                                    ContentBodyConversionService converter,
                                    UserManager userManager,
                                    LocaleResolver localeResolver,
                                    EventPublisher eventPublisher,
                                    I18nResolver i18nResolver)
    {
        accessorFactory = httpAccessor;
        this.converter = converter;
        this.userManager = userManager;
        this.localeResolver = localeResolver;
        this.eventPublisher = eventPublisher;
        this.i18nResolver = i18nResolver;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        super.init(params);
        log.debug("initializing " + BlueprintContextProvider.class.getSimpleName());

        checkContainsKey(params, CONTEXT_URL_KEY, "the " + CONTEXT_URL_KEY + " is not specified. "
                                              + "The context-provider element should not have been supplied "
                                              + "if the connect addon blueprint module did not provide a context provider url");
        checkContainsKey(params, REMOTE_ADDON_KEY, "the " + REMOTE_ADDON_KEY + " is not specified.");
        checkContainsKey(params, CONTENT_TEMPLATE_KEY, "the " + CONTENT_TEMPLATE_KEY + " is not specified");

        contextUrl = params.get(CONTEXT_URL_KEY);
        addonKey = params.get(REMOTE_ADDON_KEY);
        blueprintKey = params.get(CONTENT_TEMPLATE_KEY);
        vendorName = StringUtils.isBlank(params.get(REMOTE_VENDOR_NAME)) ? addonKey : params.get(REMOTE_VENDOR_NAME);
        pluginAccessor = accessorFactory.getOrThrow(addonKey);
    }

    private static void checkContainsKey(Map<String, String> params, String key, String message)
    {
        if (!params.containsKey(key))
        {
            throw new PluginParseException(message);
        }
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext)
    {
        log.debug("Blueprint '" + blueprintKey + "' in '" + addonKey + "' executing POST to '" + contextUrl + "'");

        Promise<String> promise = pluginAccessor.executeAsync(POST,
                                                              URI.create(contextUrl),
                                                              emptyMap(),
                                                              ImmutableMap.of(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()),
                                                              toInputStream(buildBody(blueprintContext)));
        String json = retrieveResponse(promise, blueprintContext);
        List<BlueprintContextValue> contextMap = readJsonResponse(json, blueprintContext);
        contextMap.forEach(v -> blueprintContext.put(v.getIdentifier(), transformValue(v, blueprintContext)));
        return blueprintContext;
    }

    private List<BlueprintContextValue> readJsonResponse(String json, BlueprintContext blueprintContext)
    {
        log.debug("start parsing response json into " + responseType.getTypeName());
        long start =  System.currentTimeMillis();
        List<BlueprintContextValue> contextMap = emptyList();
        String blueprintName = blueprintContext.get(BLUEPRINT_NAME) + "(" + vendorName + ")";

        try
        {

            contextMap = gson.fromJson(json, responseType);
        }
        catch (JsonSyntaxException e)
        {
            log.info(String.format("Blueprint context JSON syntax error (%s,%s,%s). %s.", addonKey, blueprintKey, contextUrl, e.getMessage()));
            if (log.isDebugEnabled())
            {
                log.debug("response: " + json);
                log.debug("", e);
            }
            else
            {
                log.info("Turn on debug for " + log.getName() + " to see the full stacktrace.");
            }
            eventPublisher.publish(new BlueprintContextResponseParseFailureEvent(addonKey, blueprintKey, contextUrl));
            throw new RuntimeException(i18nResolver.getText("create.content.plugin.plugin.templates.error-message.parse-error", blueprintName), e);
        }

        long timeTaken = System.currentTimeMillis() - start;
        if (log.isDebugEnabled())
        {
            log.debug(Arrays.toString(contextMap.toArray()));
        }
        log.debug("finish parsing response json in " + timeTaken + "ms");
        eventPublisher.publish(new BlueprintContextResponseParseSuccessEvent(addonKey, blueprintKey, contextUrl, timeTaken));
        return contextMap;
    }

    private String retrieveResponse(Promise<String> promise, BlueprintContext blueprintContext)
    {
        log.debug("start retrieving response");
        long start = System.currentTimeMillis();
        String json;
        String blueprintName = (String) blueprintContext.get(BLUEPRINT_NAME);

        try
        {
            json = promise.get(MAX_TIMEOUT, SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            log.info(String.format("Blueprint context retrieval error (%s,%s,%s). %s.", addonKey, blueprintKey, contextUrl, e.getMessage()));
            if (log.isDebugEnabled())
            {
                log.debug("", e);
            }
            else
            {
                log.info("Turn on debug for " + log.getName() + " to see the full stacktrace.");
            }
            eventPublisher.publish(new BlueprintContextRequestFailedEvent(addonKey, blueprintKey, contextUrl, e.getClass().getName()));

            if (e instanceof TimeoutException)
            {
                throw new RuntimeException(i18nResolver.getText("create.content.plugin.plugin.templates.error-message.timeout", blueprintName), e);
            }
            else
            {
                throw new RuntimeException(i18nResolver.getText("create.content.plugin.plugin.templates.error-message.generic", blueprintName), e);
            }
        }

        long timeTaken = System.currentTimeMillis() - start;
        if (log.isDebugEnabled())
        {
            log.debug(json);
        }
        log.debug("finished retrieving response in " + timeTaken + "ms");
        eventPublisher.publish(new BlueprintContextRequestSuccessEvent(addonKey, blueprintKey, contextUrl, timeTaken));
        return json;
    }

    private String transformValue(BlueprintContextValue v, BlueprintContext blueprintContext)
    {
        if (isValidForTransform(v))
        {
            String converted;
            try
            {
                converted = converter.convert(makeContentBody(v), ContentRepresentation.STORAGE).getValue();
            }
            catch (ServiceException e)
            {
                log.error(String.format("conversion to STORAGE format failed for %s: %s", v, e.getMessage()));
                if (log.isDebugEnabled())
                {
                    log.debug(String.format("(%s,%s,%s):", addonKey, blueprintKey, contextUrl), e);
                }

                // Use the error message as converted value
                String blueprintName = (String) blueprintContext.get(BLUEPRINT_NAME);
                converted = String.format("There is a problem converting variable %s in %s", v.getIdentifier(), blueprintName);
            }

            if (log.isDebugEnabled())
            {
                log.debug("converted '" + v.getValue() + "' to '" + converted + "'");
            }
            return converted;
        }
        else
        {
            return v.getValue();
        }
    }

    private boolean isValidForTransform(BlueprintContextValue v)
    {
        final ContentRepresentation representation;
        try
        {
            representation = ContentRepresentation.valueOf(v.getRepresentation());
        }
        catch (IllegalArgumentException ignored)
        {
            log.debug("Ignored blueprint context value with invalid representation: " + v);
            return false;
        }
        return Iterables.contains(ContentRepresentation.INPUT_CONVERSION_TO_STORAGE_ORDER, representation);
    }

    private ContentBody makeContentBody(BlueprintContextValue v)
    {
        return ContentBody.contentBodyBuilder()
                .representation(ContentRepresentation.valueOf(v.getRepresentation()))
                .value(v.getValue())
                .build();
    }

    @VisibleForTesting
    public String getAddonKey()
    {
        return addonKey;
    }

    @VisibleForTesting
    public String getBlueprintKey()
    {
        return blueprintKey;
    }

    @VisibleForTesting
    public String getContextUrl()
    {
        return contextUrl;
    }

    private String buildBody(BlueprintContext blueprintContext)
    {
        UserKey remoteUserKey = userManager.getRemoteUserKey();
        String userKey = remoteUserKey != null ? remoteUserKey.getStringValue() : "";
        Locale userLocale = localeResolver.getLocale(remoteUserKey);
        String bodyJson = gson.toJson(new BlueprintContextPostBody(addonKey, blueprintKey, blueprintContext.getSpaceKey(), userKey, userLocale));
        if (log.isDebugEnabled())
        {
            log.debug("sending json " + bodyJson);
        }
        return bodyJson;
    }
}