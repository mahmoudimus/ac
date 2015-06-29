package com.atlassian.plugin.connect.plugin.util;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.atlassian.oauth.util.Check.notNull;

/**
 * Abstract component who wait for major event to perform some kind of database touching.
 * The need of this come from a bug in plugins in platform 2 SAL-282, onStart is called too early.
 * go/awesome-launcher for more information.
 * After moving to platform 3, we can simply replace this by LifecycleAware.onStart()
 */
public abstract class AbstractInitializingComponent implements InitializingBean, DisposableBean, LifecycleAware
{
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final EventPublisher eventPublisher;
    @GuardedBy ("this")
    private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);

    /**
     * The plugin that contains this component.
     */
    private final String targetPluginKey;

    public AbstractInitializingComponent(final EventPublisher eventPublisher, final String targetPluginKey)
    {
        this.targetPluginKey = notNull(targetPluginKey, "targetPluginKey");
        this.eventPublisher = notNull(eventPublisher, "eventPublisher");
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event)
    {
        if (isTargetPlugin(event.getPlugin()))
        {
            onLifecycleEvent(LifecycleEvent.PLUGIN_ENABLED);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
        onLifecycleEvent(LifecycleEvent.AFTER_PROPERTIES_SET);
    }

    @Override
    public void onStart()
    {
        onLifecycleEvent(LifecycleEvent.LIFECYCLE_AWARE_ON_START);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    /**
     * Wait for both PluginEnableEvent and onStart before performing any initialization that relating to DB or external
     * service. The need of this come from a bug in plugins in platform 2 SAL-282, onStart is called too early.
     * go/awesome-launcher for more information. After platform 3, we don't need this kind of 3-2-1-GO! any more, just
     * to use onStart.
     */
    private void onLifecycleEvent(LifecycleEvent event)
    {
        logger.info("onLifecycleEvent {}", event);
        if (isLifecycleReady(event))
        {
            logger.info("Got the last lifecycle event... Time to get started!");
            eventPublisher.unregister(this);
            finalInit();
        }
    }

    protected boolean isTargetPlugin(Plugin plugin)
    {
        return (targetPluginKey.equals(plugin.getKey()));
    }

    synchronized private boolean isLifecycleReady(LifecycleEvent event)
    {
        return lifecycleEvents.add(event) && lifecycleEvents.size() == LifecycleEvent.values().length;
    }

    protected abstract void finalInit();


    static enum LifecycleEvent
    {
        AFTER_PROPERTIES_SET,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_ON_START;
    }
}
