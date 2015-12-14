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
 * Descriptor for providing movable location of web-panels.
 *
 * Movable location is a location, that content within is moved in the DOM, like locations inside AUI dropdowns.
 * Every time web-panel's iframe is moved in the DOM it do the request.
 * Because request can be done any time the jwt token may expire.
 * Because of that web-panel's iframe has to point to the redirect servlet
 * that will return a freshly signed redirection to the connect add-on server.
 *
 * This descriptor expects XML of the form:
 * <pre>
 *     {@code
 *      <movable-web-panel-locations-list>
 *          <location>...</location>
 *          <location>...</location>
 *      <movable-web-panel-locations-list>
 *     }
 * </pre>
 */
public final class MovableWebPanelLocationProviderModuleDescriptor
        extends AbstractModuleDescriptor<MovableWebPanelLocationProviderModuleDescriptor.MovableWebPanelLocationProvider>
{
    private MovableWebPanelLocationProvider module;

    public static final class MovableWebPanelLocationProvider
    {
        private final Set<String> movableLocations;

        public MovableWebPanelLocationProvider(final Set<String> movableLocations)
        {
            this.movableLocations = movableLocations;
        }

        public Set<String> getMovableLocations()
        {
            return movableLocations;
        }
    }

    public MovableWebPanelLocationProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.module = createMovableWebPanelLocationProvider(element);
    }

    private MovableWebPanelLocationProvider createMovableWebPanelLocationProvider(Element element)
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
        return new MovableWebPanelLocationProvider(locations);
    }

    @Override
    public MovableWebPanelLocationProvider getModule()
    {
        return module;
    }
}
