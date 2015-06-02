//package com.atlassian.plugin.connect.jira.capabilities.provider;
//
//import com.atlassian.plugin.ModuleDescriptor;
//import com.atlassian.plugin.Plugin;
//import com.atlassian.plugin.connect.jira.capabilities.descriptor.ConnectEntityPropertyModuleDescriptorFactory;
//import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
//import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
//import com.atlassian.plugin.connect.spi.module.Module;
//import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
//import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
//import com.atlassian.plugin.connect.spi.module.provider.TestConnectModuleProvider;
//import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
//import com.google.common.base.Function;
//import com.google.common.collect.Lists;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@JiraComponent
//public class TestEntityPropertyModuleProvider implements ConnectModuleProvider
//{
//    private final ConnectEntityPropertyModuleDescriptorFactory descriptorFactory;
//
//    @Autowired
//    public TestEntityPropertyModuleProvider(ConnectEntityPropertyModuleDescriptorFactory descriptorFactory)
//    {
//        System.out.println("HI WE'RE CREATING THE TESTENTITYPROPERTYMODULEPROVIDER");
//        this.descriptorFactory = descriptorFactory;
//    }
//
//    @Override
//    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, Plugin plugin, String jsonFieldName, final List<Module> modules)
//    {
//        return null;
//        beanToDescriptor(moduleProviderContext, bean);
//        return Lists.transform(modules, new Function<EntityPropertyModuleBean, ModuleDescriptor>()
//        {
//            @Override
//            public ModuleDescriptor apply(final Module module)
//            {
//                EntityPropertyModuleBean bean = module.toBean(EntityPropertyModuleBean.class);
//                return descriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
//            }
//        });
//    }
//}
