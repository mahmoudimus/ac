package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RedirectedWebPanelSectionSearcher
{
    private final PluginAccessor pluginAccessor;

    @Autowired
    public RedirectedWebPanelSectionSearcher(
            PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public boolean doesWebPanelNeedsToBeRedirected(WebPanelModuleBean bean, ConnectAddonBean connectAddonBean)
    {
        Set<String> redirectedWebPanelLocations = getRedirectedWebPanelLocations();
        String beanLocation = bean.getLocation();
        if (redirectedWebPanelLocations.contains(beanLocation))
        {
            return true;
        }

        List<WebSectionModuleBean> webSections = getWebSectionModuleBeans(connectAddonBean);
        return isInRedirectedWebSection(beanLocation, webSections, redirectedWebPanelLocations, new HashSet<>());
    }

    private Set<String> getRedirectedWebPanelLocations()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(RedirectedWebPanelLocationProviderModuleDescriptor.class)
                .stream()
                .flatMap(connectWebFragmentLocationBlacklist -> connectWebFragmentLocationBlacklist.getModule().getRedirectedLocations().stream())
                .collect(Collectors.toSet());
    }

    private boolean isInRedirectedWebSection(String locationKey, List<WebSectionModuleBean> webSections, Set<String> redirectedLocations, Set<String> visitedLocations)
    {
        // Location may be a section with sub sections. If web panel is in sub-section we need to check if that sub-section belongs to the locations that requires redirection.
        Optional<WebSectionModuleBean> parentSection = findParentSection(locationKey, webSections);
        if (!parentSection.isPresent())
        {
            return false;
        }

        String parentSectionLocation = parentSection.get().getLocation();

        // that prevent going in infinite cycle if there is a cycle in locations.
        if (visitedLocations.contains(parentSectionLocation)){
            return false;
        }
        visitedLocations.add(parentSectionLocation);

        if (redirectedLocations.contains(parentSectionLocation))
        {
            return true;
        }

        return isInRedirectedWebSection(parentSectionLocation, webSections, redirectedLocations, visitedLocations);
    }

    private List<WebSectionModuleBean> getWebSectionModuleBeans(ConnectAddonBean connectAddonBean)
    {
        List<ModuleBean> webSection = connectAddonBean.getModules().getValidModuleListOfType("webSections", e -> {}).orElse(Collections.emptyList());

        return webSection.stream()
                .map(moduleBean -> (WebSectionModuleBean) moduleBean)
                .collect(Collectors.toList());
    }

    private Optional<WebSectionModuleBean> findParentSection(String location, List<WebSectionModuleBean> webSections)
    {
        return webSections.stream()
                .filter(webSectionModuleBean -> webSectionModuleBean.getRawKey().equals(location))
                .findFirst();
    }
}
