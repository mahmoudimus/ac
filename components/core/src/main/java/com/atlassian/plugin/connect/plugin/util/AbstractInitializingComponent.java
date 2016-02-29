package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.concurrent.GuardedBy;
import java.util.EnumSet;
import java.util.Set;

import static com.atlassian.oauth.util.Check.notNull;

/**
 * An abstract component that waits for certain tenancy events before interacting with the database.
 * This is a workaround for a bug in SAL-282 (onStart is being called too early).
 * go.atlassian.com/awesome-launcher for more information.
 * After moving to platform 3, these methods can be replaced with an implementation of LifecycleAware.onStart()
 */
public abstract class AbstractInitializingComponent implements InitializingBean, DisposableBean, LifecycleAware {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final EventPublisher eventPublisher;
    @GuardedBy("this")
    private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);

    public AbstractInitializingComponent(final EventPublisher eventPublisher) {
        this.eventPublisher = notNull(eventPublisher, "eventPublisher");
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
        if (isTargetPlugin(event.getPlugin())) {
            onLifecycleEvent(LifecycleEvent.PLUGIN_ENABLED);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
        onLifecycleEvent(LifecycleEvent.AFTER_PROPERTIES_SET);
    }

    @Override
    public void onStart() {
        onLifecycleEvent(LifecycleEvent.LIFECYCLE_AWARE_ON_START);
    }

    @Override
    public void onStop() {
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    /**
     * Wait for both PluginEnableEvent and onStart before performing any initialization relating to the DB or external
     * services (see {@link AbstractInitializingComponent} for why we need to do this).
     */
    private void onLifecycleEvent(LifecycleEvent event) {
        logger.info("onLifecycleEvent {}", event);
        if (isLifecycleReady(event)) {
            logger.info("Got the last lifecycle event... Time to get started!");
            eventPublisher.unregister(this);
            finalInit();
        }
    }

    protected boolean isTargetPlugin(Plugin plugin) {
        return (ConnectPluginInfo.getPluginKey().equals(plugin.getKey()));
    }

    synchronized private boolean isLifecycleReady(LifecycleEvent event) {
        return lifecycleEvents.add(event) && lifecycleEvents.size() == LifecycleEvent.values().length;
    }

    protected abstract void finalInit();

    enum LifecycleEvent {
        AFTER_PROPERTIES_SET,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_ON_START
    }
}
