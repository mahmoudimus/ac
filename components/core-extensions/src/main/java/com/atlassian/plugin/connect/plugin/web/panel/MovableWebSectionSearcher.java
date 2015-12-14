package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MovableWebSectionSearcher
{
    private final PluginAccessor pluginAccessor;

    @Autowired
    public MovableWebSectionSearcher(
            PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public boolean isWebPanelInMovableWebSection(WebPanelModuleBean bean, ConnectAddonBean connectAddonBean)
    {
        Set<String> movableWebSectionLocations = getMovableWebSectionLocations();
        String beanLocation = bean.getLocation();
        if (movableWebSectionLocations.contains(beanLocation))
        {
            return true;
        }

        List<WebSectionModuleBean> webSections = getWebSectionModuleBeans(connectAddonBean);
        return isInMovableWebSection(beanLocation, webSections, movableWebSectionLocations);
    }

    private Set<String> getMovableWebSectionLocations()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(MovableWebPanelLocationProviderModuleDescriptor.class)
                .stream()
                .flatMap(new Function<MovableWebPanelLocationProviderModuleDescriptor, Stream<String>>()
                {
                    @Override
                    public Stream<String> apply(MovableWebPanelLocationProviderModuleDescriptor connectWebFragmentLocationBlacklist)
                    {
                        return connectWebFragmentLocationBlacklist.getModule().getMovableLocations().stream();
                    }
                })
                .collect(Collectors.toSet());
    }

    private boolean isInMovableWebSection(String locationKey, List<WebSectionModuleBean> webSections, Set<String> movableLocations)
    {
        // Movable sections may have sub section. If web panel is in sub-section we need to check if that sub-section belongs to the movable sections.
        Optional<WebSectionModuleBean> parentSection = findParentSection(locationKey, webSections);
        if (!parentSection.isPresent())
        {
            return false;
        }

        String parentSectionLocation = parentSection.get().getLocation();
        if (movableLocations.contains(parentSectionLocation))
        {
            return true;
        }

        return isInMovableWebSection(parentSectionLocation, webSections, movableLocations);
    }

    private List<WebSectionModuleBean> getWebSectionModuleBeans(ConnectAddonBean connectAddonBean)
    {
        List<ModuleBean> webSection = connectAddonBean.getModules().getValidModuleListOfType("webSections", new Consumer<Exception>()
        {
            @Override
            public void accept(Exception e)
            {
            }
        }).orElse(Collections.emptyList());

        return webSection.stream()
                .map(new Function<ModuleBean, WebSectionModuleBean>()
                {
                    @Override
                    public WebSectionModuleBean apply(ModuleBean moduleBean)
                    {
                        return (WebSectionModuleBean) moduleBean;
                    }
                })
                .collect(Collectors.toList());
    }

    private Optional<WebSectionModuleBean> findParentSection(String location, List<WebSectionModuleBean> webSections)
    {
        return webSections.stream().filter(new Predicate<WebSectionModuleBean>()
        {
            @Override
            public boolean test(WebSectionModuleBean webSectionModuleBean)
            {
                return webSectionModuleBean.getRawKey().equals(location);
            }
        }).findFirst();
    }
}
