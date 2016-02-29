package com.atlassian.plugin.connect.plugin.web.blacklist;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Descriptor for blacklisting web-panel and web-item locations which are not supported in Connect.
 *
 * This descriptor expects XML of the form:
 * <pre>
 *     {@code
 *      <connect-web-fragment-location-blacklist>
 *           <web-panel-locations>
 *               <location></location>
 *           </web-panel-locations>
 *           <web-item-locations>
 *               <location></location>
 *           </web-item-locations>
 *
 *      </connect-web-fragment-location-blacklist>
 *     }
 * </pre>
 */
public final class ConnectWebFragmentLocationBlacklistModuleDescriptor extends AbstractModuleDescriptor<ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist> {
    private ConnectWebFragmentLocationBlacklist module;

    public static final class ConnectWebFragmentLocationBlacklist {
        private final ImmutableSet<String> webPanelBlacklistedLocations;
        private final ImmutableSet<String> webItemBlacklistedLocations;

        public ConnectWebFragmentLocationBlacklist(ImmutableSet<String> webPanelBlacklistedLocations,
                                                   ImmutableSet<String> webItemBlacklistedLocations) {
            this.webPanelBlacklistedLocations = webPanelBlacklistedLocations;
            this.webItemBlacklistedLocations = webItemBlacklistedLocations;
        }

        public ImmutableSet<String> getWebItemBlacklistedLocations() {
            return webItemBlacklistedLocations;
        }

        public ImmutableSet<String> getWebPanelBlacklistedLocations() {
            return webPanelBlacklistedLocations;
        }
    }

    public ConnectWebFragmentLocationBlacklistModuleDescriptor(ModuleFactory moduleFactory) {
        super(moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException {
        super.init(plugin, element);
        this.module = loadBlacklist(element);
    }

    private ConnectWebFragmentLocationBlacklist loadBlacklist(Element element) {
        final ImmutableSet<String> webPanelLocations = getLocations(element, "web-panel-locations");
        final ImmutableSet<String> webItemLocations = getLocations(element, "web-item-locations");
        return new ConnectWebFragmentLocationBlacklist(webPanelLocations, webItemLocations);
    }

    private ImmutableSet<String> getLocations(Element element, String elementName) {
        Element subElementWithLocations = element.element(elementName);
        if (subElementWithLocations == null) return ImmutableSet.of();

        List<Element> locations = getElements(subElementWithLocations);
        return ImmutableSet.copyOf(locations.stream().map(Element::getText).collect(Collectors.toSet())
        );
    }

    @SuppressWarnings("unchecked")
    private List<Element> getElements(Element element) {
        return element.elements();
    }

    @Override
    public ConnectWebFragmentLocationBlacklist getModule() {
        return module;
    }
}
