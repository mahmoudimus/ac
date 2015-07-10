package it.com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

@RunWith(AtlassianPluginsTestRunner.class)
public class PluginDescriptorTest
{

    private final PluginAccessor pluginAccessor;

    public PluginDescriptorTest(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Test
    public void shouldReturnModulesForDescriptors()
    {
        final Collection<ModuleDescriptor<Object>> moduleDescriptors = pluginAccessor.getModuleDescriptors(new ModuleDescriptorPredicate<Object>()
        {
            @Override
            public boolean matches(ModuleDescriptor<?> moduleDescriptor)
            {
                return ConnectPluginInfo.getPluginKey().equals(moduleDescriptor.getPluginKey());
            }
        });
        for (ModuleDescriptor<Object> moduleDescriptor : moduleDescriptors)
        {
            if (moduleDescriptor.getKey().equals("analyticsWhitelist"))
            {
                // getModule() appears to be broken in AnalyticsWhitelistModuleDescriptor from analytics-client
                continue;
            }

            try
            {
                moduleDescriptor.getModule();
            }
            catch (UnsupportedOperationException e)
            {
                // Module types, web resources etc. do not expose classes via getModule()
            }
        }
    }
}
