package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.spi.web.MovableWebSectionKeysProvider;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovableWebSectionSearcher
{
    private final MovableWebSectionKeysProvider movableWebSectionKeysProvider;

    @Autowired
    public MovableWebSectionSearcher(final MovableWebSectionKeysProvider movableWebSectionKeysProvider)
    {
        this.movableWebSectionKeysProvider = movableWebSectionKeysProvider;
    }

    public boolean isWebPanelInMovableWebSection(final WebPanelModuleBean bean, final ConnectAddonBean connectAddonBean)
    {
        return isMovableWebSectionOrChildOfMovableWebSection(bean.getLocation(), connectAddonBean.getModules().getWebSections());
    }

    private boolean isMovableWebSectionOrChildOfMovableWebSection(final String sectionKey, List<WebSectionModuleBean> webSections)
    {
        boolean isMovableWebSection = Iterables.any(movableWebSectionKeysProvider.provide(), new Predicate<String>()
        {
            @Override
            public boolean apply(final String moveableLocation)
            {
                return sectionKey.equals(moveableLocation);
            }
        });
        if (isMovableWebSection)
        {
            return true;
        }

        // Movable sections may have sub section. If web panel is in sub-section we need to check if that sub-section belongs to the movable sections.
        Optional<WebSectionModuleBean> parentSection = findParentSection(sectionKey, webSections);
        if (!parentSection.isPresent())
        {
            return false;
        }

        final String parentSectionLocation = parentSection.get().getLocation();
        return isMovableWebSectionOrChildOfMovableWebSection(parentSectionLocation, webSections);
    }

    private Optional<WebSectionModuleBean> findParentSection(final String location, List<WebSectionModuleBean> webSections)
    {
        return Iterables.tryFind(webSections, new Predicate<WebSectionModuleBean>()
        {
            @Override
            public boolean apply(final WebSectionModuleBean webSectionModuleBean)
            {
                return webSectionModuleBean.getRawKey().equals(location);
            }
        });
    }
}
