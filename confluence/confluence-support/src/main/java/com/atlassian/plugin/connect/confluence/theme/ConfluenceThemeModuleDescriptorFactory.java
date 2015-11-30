package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.BasicTheme;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProviderContext;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.net.RequestFactory;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceThemeModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConfluenceThemeModuleBean, ThemeModuleDescriptor>
{
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
    public ThemeModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext,
                                                        Plugin plugin,
                                                        ConfluenceThemeModuleBean bean)
    {
        ConnectAddonBean addon = moduleProviderContext.getConnectAddonBean();

        Element dom = new DOMElement("theme");
        dom.addAttribute("key", ConfluenceThemeUtils.getThemeKey(addon, bean));
        /*TODO: see if name really needs to be i18n-able. can we get away without it?*/
        dom.addAttribute("name", i18nBeanFactory.getI18NBean().getText(bean.getName().getKeyOrValue()));
        dom.addAttribute("class", BasicTheme.class.getName());
        dom.addAttribute("disable-sitemesh", "true");

        /*TODO: create an override registry*/
        for (UiOverrideBean uiOverrideBean : bean.getOverrides())
        {
            LayoutType layoutType = LayoutType.valueOf(uiOverrideBean.getType());
            dom.addElement("xwork-velocity-result-override")
               .addAttribute("package", layoutType.getPackageToOverride())
               .addAttribute("action", layoutType.getActionToOverride())
               .addAttribute("result", layoutType.getResultToOverride())
               .addAttribute("override", layoutType.getTemplateLocation());
        }

//
//        dom.addElement("param")
//           .addAttribute("name", THEME_URL_KEY)
//           .addAttribute("value", contextUrl);
//        dom.addElement("param")
//           .addAttribute("name", ADDON_KEY)
//           .addAttribute("value", addon.getKey());
//        dom.addElement("param")
//           .addAttribute("name", CONTENT_TEMPLATE_KEY)
//           .addAttribute("value", contentTemplateKey);

        ThemeModuleDescriptor themeModuleDescriptor = new ThemeModuleDescriptor(moduleFactory, pluginAccessor);
        themeModuleDescriptor.init(plugin, dom);
        return themeModuleDescriptor;
    }
}
