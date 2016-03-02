package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.LayoutModuleDescriptor;
import com.atlassian.confluence.themes.ThemedDecorator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceLayoutModuleFactory implements ConnectModuleDescriptorFactory<ConfluenceThemeModuleBean, LayoutModuleDescriptor> {
    private static final Logger log = LoggerFactory.getLogger(ConfluenceLayoutModuleFactory.class);
    private final ModuleFactory moduleFactory;

    @Autowired
    public ConfluenceLayoutModuleFactory(ModuleFactory moduleFactory) {
        this.moduleFactory = moduleFactory;
    }

    @Override
    public LayoutModuleDescriptor createModuleDescriptor(ConfluenceThemeModuleBean bean,
                                                         ConnectAddonBean addon,
                                                         Plugin plugin) {
        return createModuleDescriptor(addon, plugin, bean, NavigationTargetOverrideInfo.dashboard);
    }


    public LayoutModuleDescriptor createModuleDescriptor(ConnectAddonBean addon,
                                                         Plugin plugin,
                                                         ConfluenceThemeModuleBean bean,
                                                         NavigationTargetOverrideInfo overrideInfo) {
        Element dom = new DOMElement("layout");
        dom.addAttribute("key", ConfluenceThemeUtils.getLayoutKey(addon, bean, overrideInfo));
        dom.addAttribute("name", ConfluenceThemeUtils.getLayoutName(addon, bean, overrideInfo));
        dom.addAttribute("class", ConnectConfluenceThemeDecorator.class.getName());
        dom.addAttribute("overrides", overrideInfo.getDecoratorToOverride());
        dom.addElement("resource")
           .addAttribute("type", "velocity")
           .addAttribute("name", "decorator")
           .addAttribute("location", overrideInfo.getDecoratorLocation());

        LayoutModuleDescriptor layoutModuleDescriptor = new ConnectLayoutModuleDescriptor(moduleFactory);
        layoutModuleDescriptor.init(plugin, dom);

        if (log.isDebugEnabled()) {
            log.debug(Dom4jUtils.printNode(dom));
        }

        return layoutModuleDescriptor;
    }

    //this class hacks around bug : https://ecosystem.atlassian.net/browse/PLUG-1177
    private static class ConnectLayoutModuleDescriptor extends LayoutModuleDescriptor {
        private Class<? extends ThemedDecorator> hackedModuleClazz;

        ConnectLayoutModuleDescriptor(ModuleFactory moduleFactory) {
            super(moduleFactory);
        }

        @Override
        @SuppressWarnings({"unchecked", "RefusedBequest"})
        public Class<ThemedDecorator> getModuleClass() {
            //deliberately don't call super.getModuleClass() - that fails when this module is registered dynamically
            if (hackedModuleClazz == null) {
                hackedModuleClazz = moduleFactory.createModule(getModuleClassName(), this).getClass();
            }
            return (Class<ThemedDecorator>) hackedModuleClazz;
        }
    }

}
