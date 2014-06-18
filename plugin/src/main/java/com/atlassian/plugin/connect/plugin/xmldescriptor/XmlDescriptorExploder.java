package com.atlassian.plugin.connect.plugin.xmldescriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.spi.event.XmlDescriptorCodeInvokedEvent;
import com.atlassian.sal.api.features.DarkFeatureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class XmlDescriptorExploder
{
    private static final AtomicReference<XmlDescriptorExploder> instanceRef = new AtomicReference<XmlDescriptorExploder>();
    private static final String DARK_FEATURE_PREFIX = "connect.xmldescriptor.";
    private static final String DARK_FEATURE_NOTIFY = DARK_FEATURE_PREFIX + "notify";
    private static final String DARK_FEATURE_EXPLODE = DARK_FEATURE_PREFIX + "explode";
    private static final Logger log = LoggerFactory.getLogger(XmlDescriptorExploder.class);

    private final DarkFeatureManager darkFeatureManager;
    private final EventPublisher eventPublisher;

    @Inject
    public XmlDescriptorExploder(DarkFeatureManager darkFeatureManager, EventPublisher eventPublisher)
    {
        this.darkFeatureManager = darkFeatureManager;
        this.eventPublisher = eventPublisher;

        if (!instanceRef.compareAndSet(null, this))
        {
            log.warn(getClass().getSimpleName() + " would ideally not be instantiated more than once. This is at least the second instantiation.");
        }
    }

    public static void notifyAndExplode(String addOnKey)
    {
        XmlDescriptorExploder instance = instanceRef.get();

        if (null == instance)
        {
            throw new IllegalStateException("A " + XmlDescriptorExploder.class.getSimpleName() + " instance must be constructed before notifyAndExplode() is called.");
        }

        instance.notifyAndExplodeInternal(addOnKey, Thread.currentThread().getStackTrace());
    }

    private void notifyAndExplodeInternal(String addOnKey, StackTraceElement[] stackTrace)
    {
        if (darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_NOTIFY))
        {
            eventPublisher.publish(new XmlDescriptorCodeInvokedEvent(addOnKey, stackTrace));
        }

        if (darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_EXPLODE))
        {
            throw new RuntimeException("The Connect XML descriptor is unsupported. Deprecation was announced in February 2014 for May 2014. Please see <https://developer.atlassian.com/static/connect/docs/resources/deprecations.html> for more information.");
        }
    }
}
