package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevelModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.plugin.util.concurrent.CopyOnWriteMap;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
@Component
public class AccessLevelManager implements DisposableBean
{
    private final PluginModuleTracker<AccessLevel, AccessLevelModuleDescriptor> accessLevelTracker;
    private final Map<String,AccessLevel> accessLevels = CopyOnWriteMap.newHashMap();

    @Autowired
    public AccessLevelManager(PluginEventManager pluginEventManager, PluginAccessor pluginAccessor)
    {
        this.accessLevelTracker = new DefaultPluginModuleTracker<AccessLevel, AccessLevelModuleDescriptor>(pluginAccessor, pluginEventManager,
                AccessLevelModuleDescriptor.class, new PluginModuleTracker.Customizer<AccessLevel,AccessLevelModuleDescriptor>()
        {

            @Override
            public AccessLevelModuleDescriptor adding(AccessLevelModuleDescriptor descriptor)
            {
                AccessLevel module = descriptor.getModule();
                accessLevels.put(module.getId(), module);
                return descriptor;
            }

            @Override
            public void removed(AccessLevelModuleDescriptor descriptor)
            {
                accessLevels.remove(descriptor.getModule().getId());
            }
        });
    }

    public AccessLevel getAccessLevel(final String value)
    {
        final AccessLevel result = accessLevels.get(value);
        if (result != null)
        {
            return result;
        }
        else
        {
            final AtomicReference<AccessLevel> ref = new AtomicReference<AccessLevel>();
            // wait 10 seconds for one to show up (necessary during restart)
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    for (int x = 0; x<100; x++)
                    {
                        AccessLevel result = accessLevels.get(value);
                        if (result != null)
                        {
                            ref.set(result);
                            break;
                        }
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            // no worries
                        }
                    }
                }
            });
            t.start();
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                // no worries
            }
            if (ref.get() != null)
            {
                return ref.get();
            }
        }
        throw new IllegalArgumentException("Invalid access level '" + value + "'");
    }

    @Override
    public void destroy() throws Exception
    {
        accessLevelTracker.close();
    }
}
