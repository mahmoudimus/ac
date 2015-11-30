package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.LayoutModuleDescriptor;
import com.atlassian.confluence.themes.ThemedDecorator;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.net.RequestFactory;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceLayoutModuleFactory implements ConnectModuleDescriptorFactory<ConfluenceThemeModuleBean, LayoutModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceLayoutModuleFactory.class);
    private final ModuleFactory moduleFactory;
    private final I18NBeanFactory i18nBeanFactory;
    private final RequestFactory<?> requestFactory;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public ConfluenceLayoutModuleFactory(ModuleFactory moduleFactory,
                                         I18NBeanFactory i18nBeanFactory,
                                         RequestFactory<?> requestFactory,
                                         PluginAccessor pluginAccessor) {
        this.moduleFactory = moduleFactory;
        this.i18nBeanFactory = i18nBeanFactory;
        this.requestFactory = requestFactory;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public LayoutModuleDescriptor createModuleDescriptor(ConfluenceThemeModuleBean bean,
                                                         ConnectAddonBean addon,
                                                         Plugin plugin)
    {
        return createModuleDescriptor(addon, plugin, bean, LayoutType.main);
    }


    public LayoutModuleDescriptor createModuleDescriptor(ConnectAddonBean addon,
                                                         Plugin plugin,
                                                         ConfluenceThemeModuleBean bean,
                                                         LayoutType type)
    {
        Element dom = new DOMElement("layout");
        dom.addAttribute("key", ConfluenceThemeUtils.getLayoutKey(addon, bean, type));
        dom.addAttribute("name", ConfluenceThemeUtils.getLayoutName(addon, bean, type));
        dom.addAttribute("class", ConnectThemeDecorator.class.getName());
        dom.addAttribute("overrides", type.getDecoratorToOverride());
        dom.addElement("resource")
           .addAttribute("type", "velocity")
           .addAttribute("name", "decorator")
           .addAttribute("location", type.getDecoratorLocation());

        LayoutModuleDescriptor layoutModuleDescriptor = new MyLayoutModuleDescriptor(moduleFactory);
        layoutModuleDescriptor.init(plugin, dom);

        if (log.isDebugEnabled())
        {
            log.debug(Dom4jUtils.printNode(dom));
        }

        return layoutModuleDescriptor;
    }

    private static class MyLayoutModuleDescriptor extends LayoutModuleDescriptor
    {
        private Class<? extends ThemedDecorator> hackedModuleClazz;

        MyLayoutModuleDescriptor(ModuleFactory moduleFactory)
        {
            super(moduleFactory);
        }

        @Override
        public Class<ThemedDecorator> getModuleClass()
        {
            if (hackedModuleClazz == null)
            {
                hackedModuleClazz = moduleFactory.createModule(getModuleClassName(), this).getClass();
            }
            return (Class<ThemedDecorator>) hackedModuleClazz;
        }
    }

}
