package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.HttpHeaderNames;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.plugin.util.http.ContentRetrievalException;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.AddOnConditionFailedEvent;
import com.atlassian.plugin.connect.spi.event.AddOnConditionInvokedEvent;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.Promise;
import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A condition that evaluates based on the response from a remote end-point hosted by the add-on.
 */
public class AddOnCondition implements Condition
{
    public static final String URL = "url";
    public static final String ADDON_KEY = "addOnKey";

    private static final Logger log = LoggerFactory.getLogger(AddOnCondition.class);

    private interface Configuration
    {
        String getUrl();

        String getAddOnKey();
    }

    private final AtomicReference<Configuration> configuration = new AtomicReference<Configuration>();

    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final EventPublisher eventPublisher;
    private BundleContext bundleContext;

    public AddOnCondition(final RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                          final IFrameUriBuilderFactory iFrameUriBuilderFactory,
                          final PluggableParametersExtractor webFragmentModuleContextExtractor,
                          EventPublisher eventPublisher, BundleContext bundleContext)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
        this.eventPublisher = eventPublisher;
        this.bundleContext = bundleContext;
    }

    /**
     * Initializer used when created as a plugins2 web fragment condition.
     */
    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        Configuration cfg = new ConfigurationImpl(
                checkNotNull(params.get(ADDON_KEY), ADDON_KEY),
                checkNotNull(params.get(URL), URL)
        );
        configuration.set(cfg);
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Configuration cfg = checkNotNull(configuration.get(), "configuration has not been set - init() not called?");

        ModuleContextParameters moduleContext = webFragmentModuleContextExtractor.extractParameters(context);

        String uriString = iFrameUriBuilderFactory
                .builder()
                .addOn(cfg.getAddOnKey())
                .namespace("condition") // namespace is not really important as we're not rendering an iframe
                .urlTemplate(cfg.getUrl())
                .context(moduleContext)
                .sign(false)
                .build();

        final URI uri = URI.create(uriString);
        final String uriPath = uri.getPath();
        final String version = BundleUtil.getBundleVersion(bundleContext);
        final Map<String, String> httpHeaders = Collections.singletonMap(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION,
                version);
        Promise<String> responsePromise = remotablePluginAccessorFactory.getOrThrow(cfg.getAddOnKey())
                .executeAsync(HttpMethod.GET, uri,
                        Collections.<String, String[]>emptyMap(), httpHeaders);

        String response;
        try
        {
            response = responsePromise.claim();
        }
        catch (Exception e)
        {
            final long elapsedMillisecs = stopWatch.getTime();
            final String message = String.format(String.format("Request to addon condition URL failed: %s", cfg));
            log.warn(message, e);
            eventPublisher.publish(new AddOnConditionFailedEvent(cfg.getAddOnKey(), uriPath, elapsedMillisecs, getErrorMessage(e, message)));
            return false;
        }

        try
        {
            final long elapsedMillisecs = stopWatch.getTime();
            JSONObject obj = (JSONObject) JSONValue.parseWithException(response);
            Object shouldDisplayObj = obj.get("shouldDisplay");

            Boolean shouldDisplay = getShouldDisplay(shouldDisplayObj);
            if (shouldDisplay != null)
            {
                eventPublisher.publish(new AddOnConditionInvokedEvent(cfg.getAddOnKey(), uriPath, elapsedMillisecs));
                return shouldDisplay;
            }
            else
            {
                final String message = String.format("Malformed response from addon condition URL: %s\nExpected a boolean value "
                        + "but was " + shouldDisplayObj, cfg);
                log.warn(message);
                eventPublisher.publish(new AddOnConditionFailedEvent(cfg.getAddOnKey(), uriPath, elapsedMillisecs, message));
                return false;
            }
        }
        catch (Exception e)
        {
            final long elapsedMillisecs = stopWatch.getTime();
            final String message = String.format("Malformed response from addon condition URL: %s", cfg);
            log.warn(message, e);
            eventPublisher.publish(new AddOnConditionFailedEvent(cfg.getAddOnKey(), uriPath, elapsedMillisecs, getErrorMessage(e, message)));
            return false;
        }
    }

    private Boolean getShouldDisplay(Object shouldDisplayObj)
    {
        Boolean shouldDisplay = null;
        if (shouldDisplayObj instanceof Boolean)
        {
            shouldDisplay = (Boolean) shouldDisplayObj;
        }
        else if (shouldDisplayObj instanceof String)
        {
            shouldDisplay = Boolean.parseBoolean((String) shouldDisplayObj);
        }
        return shouldDisplay;
    }

    private static final class ConfigurationImpl implements Configuration
    {
        private final String addonKey;
        private final String url;

        private ConfigurationImpl(String addonKey, String url)
        {
            this.addonKey = addonKey;
            this.url = url;
        }

        public String getAddOnKey()
        {
            return addonKey;
        }

        public String getUrl()
        {
            return url;
        }

        @Override
        public String toString()
        {
            return String.format("Condition for %s at %s", addonKey, url);
        }
    }

    private static String getErrorMessage(Exception e, String defaultMessage)
    {
        if (e instanceof ContentRetrievalException)
        {
            ContentRetrievalException cre = (ContentRetrievalException) e;
            return cre.getErrors().getMessages().toString();
        }
        if (e.getMessage() != null)
        {
            return e.getMessage();
        }
        if (e.getCause() != null && e.getCause().getMessage() != null)
        {
            return e.getCause().getMessage();
        }
        return defaultMessage;
    }
}
