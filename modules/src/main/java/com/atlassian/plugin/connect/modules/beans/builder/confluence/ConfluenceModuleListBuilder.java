package com.atlassian.plugin.connect.modules.beans.builder.confluence;

import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ModuleListBuilder;
import com.atlassian.plugin.connect.modules.beans.confluence.ConfluenceModuleList;

public class ConfluenceModuleListBuilder<T extends ConfluenceModuleListBuilder,
        M extends ConfluenceModuleList> extends ModuleListBuilder<T, M>
{

    @Override
    protected M createEmpty()
    {
        return (M) new ConfluenceModuleList(); // TODO: fix once we removed JiraConfluenceModuleListBuilder
    }

    @Override
    public M build()
    {
        return modules;
    }


    public T withWebItems(WebItemModuleBean... beans)
    {
        return super.withWebItems(beans);
    }

    public T withWebPanels(WebPanelModuleBean... beans)
    {
        return super.withWebPanels(beans);
    }

    public T withWebSections(WebSectionModuleBean... beans)
    {
        return super.withWebSections(beans);
    }

    public T withWebHooks(WebHookModuleBean... beans)
    {
        return super.withWebHooks(beans);
    }

    public T withGeneralPages(ConnectPageModuleBean... beans)
    {
        return super.withGeneralPages(beans);
    }

    public T withAdminPages(ConnectPageModuleBean... beans)
    {
        return super.withAdminPages(beans);
    }

    public T withProfilePages(ConnectPageModuleBean... beans)
    {
        return super.withProfilePages(beans);
    }

    public T withConfigurePage(ConnectPageModuleBean bean)
    {
        return super.withConfigurePage(bean);
    }

    public T withDynamicContentMacros(DynamicContentMacroModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("dynamicContentMacros", beans);
    }

    public T withSpaceToolsTabs(SpaceToolsTabModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("spaceToolsTabs", beans);
    }

    public T withStaticContentMacros(StaticContentMacroModuleBean... beans)
    {
        // TODO: temp impl until withModules removed
        return withModules("staticContentMacros", beans);
    }

}
