package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.LayoutModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.Theme;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
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
public class ConfluenceThemeModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConfluenceThemeModuleBean, ThemeModuleDescriptor> {
    static final String ADDON_KEY_PROPERTY_KEY = "addon-key";
    static final String THEME_MODULE_KEY_PROPERTY_KEY = "theme-key";
    private static final Logger log = LoggerFactory.getLogger(ConfluenceThemeModuleDescriptorFactory.class);
    private static final String THEME_ICON_NAME = "themeicon.gif";
    private final ModuleFactory moduleFactory;
    private final I18NBeanFactory i18nBeanFactory;
    private final RequestFactory<?> requestFactory;
    private final PluginAccessor pluginAccessor;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;


    @Autowired
    public ConfluenceThemeModuleDescriptorFactory(ModuleFactory moduleFactory,
                                                  I18NBeanFactory i18nBeanFactory,
                                                  RequestFactory<?> requestFactory,
                                                  PluginAccessor pluginAccessor,
                                                  IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                                  IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory) {
        this.moduleFactory = moduleFactory;
        this.i18nBeanFactory = i18nBeanFactory;
        this.requestFactory = requestFactory;
        this.pluginAccessor = pluginAccessor;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
    }

    @Override
    public ThemeModuleDescriptor createModuleDescriptor(ConfluenceThemeModuleBean themeBean,
                                                        ConnectAddonBean addon,
                                                        Plugin plugin) {

        return createModuleDescriptor(themeBean, addon, plugin, Collections.emptyList());
    }

    public ThemeModuleDescriptor createModuleDescriptor(ConfluenceThemeModuleBean themeBean,
                                                        ConnectAddonBean addon,
                                                        Plugin plugin,
                                                        List<LayoutModuleDescriptor> layouts) {
        Element dom = new DOMElement("theme");
        dom.addAttribute("key", ConfluenceThemeUtils.getThemeKey(addon, themeBean));
        /*TODO: see if name really needs to be i18n-able. can we get away without it?*/
        dom.addAttribute("name", i18nBeanFactory.getI18NBean().getText(themeBean.getName().getKeyOrValue()));
        dom.addAttribute("class", ConfluenceRemoteAddonTheme.class.getName());
        dom.addAttribute("disable-sitemesh", "false");
        dom.addElement("resource").addAttribute("name", THEME_ICON_NAME)
           .addAttribute("type", "download")
           .addAttribute("location", addon.getBaseUrl() + themeBean.getIcon().getUrl());

        /*TODO: create an override registry*/
        final ConfluenceThemeRouteInterceptionsBean routes = themeBean.getRoutes();
        for (PropertyDescriptor prop : ConfluenceThemeUtils.filterProperties(routes)) {
            ConfluenceThemeRouteBean routeBean = ConfluenceThemeUtils.getRouteBeanFromProperty(routes, prop);
            String navTargetName = prop.getName();
            for (NavigationTargetOverrideInfo overrideInfo : NavigationTargetName.forNavigationTargetName(navTargetName)) {
                dom.addElement("xwork-velocity-result-override")
                   .addAttribute("package", overrideInfo.getPackageToOverride())
                   .addAttribute("action", overrideInfo.getActionToOverride())
                   .addAttribute("result", overrideInfo.getResultToOverride())
                   .addAttribute("override", overrideInfo.getTemplateLocation());
                dom.addElement("param")
                   .addAttribute("name", ConfluenceThemeUtils.getOverrideTypeName(overrideInfo))
                   .addAttribute("value", routeBean.getUrl());

                IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                                                                                        .addon(addon.getKey())
                                                                                        .module(themeBean.getRawKey())
                                                                                        .genericBodyTemplate()
                                                                                        .urlTemplate(routeBean.getUrl())
                                                                                        .ensureUniqueNamespace(false)
                                                                                        .dimensions("100%", "100%")
                                                                                        .sign(true)
                                                                                        .build();
                iFrameRenderStrategyRegistry.register(addon.getKey(),
                                                      themeBean.getRawKey(),
                                                      overrideInfo.name(),
                                                      renderStrategy);
            }
        }
        for (LayoutModuleDescriptor layout : layouts) {
            dom.addElement("layout")
               //TODO: dont hardcode this plugin key here
               .addAttribute("key", "com.atlassian.plugins.atlassian-connect-plugin:" + layout.getKey());
        }

        dom.addElement("param")
           .addAttribute("name", ADDON_KEY_PROPERTY_KEY)
           .addAttribute("value", addon.getKey());
        dom.addElement("param")
           .addAttribute("name", THEME_MODULE_KEY_PROPERTY_KEY)
           .addAttribute("value", themeBean.getRawKey());

        ThemeModuleDescriptor themeModuleDescriptor = new ConnectThemeModuleDescriptor(moduleFactory, pluginAccessor);
        themeModuleDescriptor.init(plugin, dom);

        if (log.isDebugEnabled()) {
            log.debug(Dom4jUtils.printNode(dom));
        }

        return themeModuleDescriptor;
    }

    //this class hacks around bug : https://ecosystem.atlassian.net/browse/PLUG-1177
    private static class ConnectThemeModuleDescriptor extends ThemeModuleDescriptor {
        private Class<? extends Theme> hackedModuleClazz;

        public ConnectThemeModuleDescriptor(final ModuleFactory moduleFactory, final PluginAccessor pluginAccessor) {
            super(moduleFactory, pluginAccessor);
        }

        @Override
        public Class<Theme> getModuleClass() {
            if (hackedModuleClazz == null) {
                hackedModuleClazz = moduleFactory.createModule(getModuleClassName(), this).getClass();
            }
            return (Class<Theme>) hackedModuleClazz;
        }
    }
}
