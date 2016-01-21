package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Descriptor for providing redirected location of web-panels.
 *
 * Redirected location is a location where web-panel's iframe will do requests through redirect-servlet that would sign them using JWT.
 * Example of that type localisation is an AUI drop-down, that moves its content in the DOM each time is popped up.
 * Every time web-panel's iframe is moved in the DOM it do the request.
 * User can pop up drop-down at any time, so the jwt token may expire, therefore web-panel's iframe has to point to the redirect servlet.
 *
 * This descriptor expects XML of the form:
 * <pre>
 *     {@code
 *      <redirected-web-panel-location-list>
 *          <location>...</location>
 *          <location>...</location>
 *      <redirected-web-panel-location-list>
 *     }
 * </pre>
 */
public final class RedirectedWebPanelLocationProviderModuleDescriptor
        extends AbstractModuleDescriptor<RedirectedWebPanelLocationProviderModuleDescriptor.RedirectedWebPanelLocationProvider>
{
    private RedirectedWebPanelLocationProvider module;

    public static final class RedirectedWebPanelLocationProvider
    {
        private final Set<String> redirectedLocations;

        public RedirectedWebPanelLocationProvider(final Set<String> redirectedLocations)
        {
            this.redirectedLocations = redirectedLocations;
        }

        public Set<String> getRedirectedLocations()
        {
            return redirectedLocations;
        }
    }

    public RedirectedWebPanelLocationProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.module = createRedirectedWebPanelLocationProvider(element);
    }

    private RedirectedWebPanelLocationProvider createRedirectedWebPanelLocationProvider(Element element)
    {
        // noinspection unchecked
        List<Element> locationElements = element.elements("location");
        final Set<String> locations = ImmutableSet.copyOf(locationElements.stream()
                .map(new java.util.function.Function<Element, String>()
                {
                    @Override
                    public String apply(Element location)
                    {
                        return location.getText();
                    }
                })
                .collect(Collectors.toSet()));
        return new RedirectedWebPanelLocationProvider(locations);
    }

    @Override
    public RedirectedWebPanelLocationProvider getModule()
    {
        return module;
    }
}
