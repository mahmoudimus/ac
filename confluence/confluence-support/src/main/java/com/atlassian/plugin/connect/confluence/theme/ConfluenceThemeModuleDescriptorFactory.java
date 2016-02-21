package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.LayoutModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.Theme;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.net.RequestFactory;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceThemeModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConfluenceThemeModuleBean, ThemeModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceThemeModuleDescriptorFactory.class);
    private static final String THEME_ICON_NAME = "themeicon.gif";
    static final String ADDON_KEY_PROPERTY_KEY = "addon-key";
    static final String THEME_MODULE_KEY_PROPERTY_KEY = "theme-key";

    private final ModuleFactory moduleFactory;
    private final I18NBeanFactory i18nBeanFactory;
    private final RequestFactory<?> requestFactory;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public ConfluenceThemeModuleDescriptorFactory(ModuleFactory moduleFactory,
                                                  I18NBeanFactory i18nBeanFactory,
                                                  RequestFactory<?> requestFactory,
                                                  PluginAccessor pluginAccessor)
    {
        this.moduleFactory = moduleFactory;
        this.i18nBeanFactory = i18nBeanFactory;
        this.requestFactory = requestFactory;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public ThemeModuleDescriptor createModuleDescriptor(ConfluenceThemeModuleBean bean,
                                                        ConnectAddonBean addon,
                                                        Plugin plugin)
    {

        return createModuleDescriptor(bean, addon, plugin, Collections.emptyList());
    }

    public ThemeModuleDescriptor createModuleDescriptor(ConfluenceThemeModuleBean bean,
                                                        ConnectAddonBean addon,
                                                        Plugin plugin,
                                                        List<LayoutModuleDescriptor> layouts)
    {
        Element dom = new DOMElement("theme");
        dom.addAttribute("key", ConfluenceThemeUtils.getThemeKey(addon, bean));
        /*TODO: see if name really needs to be i18n-able. can we get away without it?*/
        dom.addAttribute("name", i18nBeanFactory.getI18NBean().getText(bean.getName().getKeyOrValue()));
        dom.addAttribute("class", ConfluenceRemoteAddonTheme.class.getName());
        dom.addAttribute("disable-sitemesh", "false");
        dom.addElement("resource").addAttribute("name", THEME_ICON_NAME)
                        .addAttribute("type", "download")
                        .addAttribute("location", addon.getBaseUrl() + bean.getIcon().getUrl());

        /*TODO: create an override registry*/
        final ConfluenceThemeRouteInterceptionsBean routes = bean.getRoutes();
        List<PropertyDescriptor> routeNames = ConfluenceThemeUtils.filterProperties(routes);
        for (PropertyDescriptor prop : routeNames)
        {
            ConfluenceThemeRouteBean confluenceThemeRouteBean = ConfluenceThemeUtils.getRouteBeanFromProperty(routes, prop);
            String navigationTargetName = prop.getName();
            List<NavigationTargetOverrideInfo> navigationTargetOverrideInfo = NavigationTargetName.forNavigationTargetName(navigationTargetName);
            for (NavigationTargetOverrideInfo targetOverrideInfo : navigationTargetOverrideInfo)
            {
                dom.addElement("xwork-velocity-result-override")
                   .addAttribute("package", targetOverrideInfo.getPackageToOverride())
                   .addAttribute("action", targetOverrideInfo.getActionToOverride())
                   .addAttribute("result", targetOverrideInfo.getResultToOverride())
                   .addAttribute("override", targetOverrideInfo.getTemplateLocation());
                dom.addElement("param")
                   .addAttribute("name", ConfluenceThemeUtils.getOverrideTypeName(targetOverrideInfo))
                   .addAttribute("value", confluenceThemeRouteBean.getUrl());
            }
        }
        for (LayoutModuleDescriptor layout : layouts)
        {
            dom.addElement("layout")
               //TODO: dont hardcode this plugin key here
               .addAttribute("key", "com.atlassian.plugins.atlassian-connect-plugin:" + layout.getKey());
        }

        dom.addElement("param")
           .addAttribute("name",  ADDON_KEY_PROPERTY_KEY)
           .addAttribute("value", addon.getKey());
        dom.addElement("param")
           .addAttribute("name", THEME_MODULE_KEY_PROPERTY_KEY)
           .addAttribute("value", bean.getRawKey());

        ThemeModuleDescriptor themeModuleDescriptor = new ConnectThemeModuleDescriptor(moduleFactory, pluginAccessor);
        themeModuleDescriptor.init(plugin, dom);

        if (log.isDebugEnabled())
        {
            log.debug(Dom4jUtils.printNode(dom));
        }

        return themeModuleDescriptor;
    }

    //this class hacks around bug : https://ecosystem.atlassian.net/browse/PLUG-1177
    private static class ConnectThemeModuleDescriptor extends ThemeModuleDescriptor
    {
        private Class<? extends Theme> hackedModuleClazz;

        public ConnectThemeModuleDescriptor(final ModuleFactory moduleFactory, final PluginAccessor pluginAccessor)
        {
            super(moduleFactory, pluginAccessor);
        }

        @Override
        public Class<Theme> getModuleClass()
        {
            if (hackedModuleClazz == null)
            {
                hackedModuleClazz = moduleFactory.createModule(getModuleClassName(), this).getClass();
            }
            return (Class<Theme>) hackedModuleClazz;
        }
    }
}
