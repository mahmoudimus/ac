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
 * A web panel in a location specified in this list will perform all requests through a redirect servlet.
 * This is necessary when the web panel's iframe might be moved in the DOM. For example, an AUI dropdown
 * might move its content in the DOM every time it's opened.
 * Every time a web panel's iframe is moved it will perform a request to the add-on server.
 * However, the user could open the dropdown at any time, so the JWT token may have expired.
 * Therefore the web panel's iframe has to point to a redirect servlet that will sign the request with a new JWT token.
 *
 * This descriptor expects XML of the form:
 * <pre>
 *     {@code
 *      <connect-redirected-web-panel-location-list>
 *          <location>...</location>
 *          <location>...</location>
 *      </connect-redirected-web-panel-location-list>
 *     }
 * </pre>
 */
public final class RedirectedWebPanelLocationProviderModuleDescriptor
        extends AbstractModuleDescriptor<RedirectedWebPanelLocationProviderModuleDescriptor.RedirectedWebPanelLocationProvider> {
    private RedirectedWebPanelLocationProvider module;

    public static final class RedirectedWebPanelLocationProvider {
        private final Set<String> redirectedLocations;

        public RedirectedWebPanelLocationProvider(final Set<String> redirectedLocations) {
            this.redirectedLocations = redirectedLocations;
        }

        public Set<String> getRedirectedLocations() {
            return redirectedLocations;
        }
    }

    public RedirectedWebPanelLocationProviderModuleDescriptor(final ModuleFactory moduleFactory) {
        super(moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException {
        super.init(plugin, element);
        this.module = createRedirectedWebPanelLocationProvider(element);
    }

    private RedirectedWebPanelLocationProvider createRedirectedWebPanelLocationProvider(Element element) {
        // noinspection unchecked
        List<Element> locationElements = element.elements("location");
        final Set<String> locations = ImmutableSet.copyOf(locationElements.stream()
                .map(Element::getText)
                .collect(Collectors.toSet()));
        return new RedirectedWebPanelLocationProvider(locations);
    }

    @Override
    public RedirectedWebPanelLocationProvider getModule() {
        return module;
    }
}
