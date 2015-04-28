package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebHookModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.AutowireWithConnectPluginDecorator;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;

import java.util.Arrays;
import java.util.HashSet;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class BeanToModuleRegistrarTest
{
    private BeanToModuleRegistrar beanToModuleRegistrar;

    @Mock private DynamicDescriptorRegistration dynamicDescriptorRegistration;
    @Mock private PluginRetrievalService pluginRetrievalService;
    @Mock private ProductAccessor productAccessor;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private WebHookScopeService webHookScopeService;

    private ConnectAddonBean connectAddonBean;
    private LifecycleBean lifecycleBean;
    private ModuleList moduleList;
    
    @Mock private OsgiPlugin plugin;
    @Mock private Bundle bundle;
    @Mock private AutowireWithConnectPluginDecorator theConnectPlugin;
    @Mock private ContainerAccessor containerAccessor;
    

    private static final String EVENT_IN_SCOPES = "event in scopes";

    @Before
    public void beforeEachTest()
    {
        this.connectAddonBean = newConnectAddonBean()
                .withLifecycle(
                        newLifecycleBean().build()
                )
                .withModules("webhooks",new ModuleBean[]{})
                .withKey("a plugin key")
                .build();
        
        when(plugin.getBundle()).thenReturn(bundle);
        when(plugin.getKey()).thenReturn("a plugin key");
        when(applicationProperties.getDisplayName()).thenReturn("JIRA");
        when(pluginRetrievalService.getPlugin()).thenReturn(theConnectPlugin);
        when(theConnectPlugin.getContainerAccessor()).thenReturn(containerAccessor);
        when(webHookScopeService.getRequiredScope(EVENT_IN_SCOPES)).thenReturn(ScopeName.ADMIN);
        
        
        beanToModuleRegistrar = new BeanToModuleRegistrar(dynamicDescriptorRegistration, pluginRetrievalService,
                applicationProperties);
    }

    @Test
    public void canRegisterAddOnWithNoWebHooks()
    {
        beanToModuleRegistrar.registerDescriptorsForBeans(connectAddonBean);
    }

    @Test
    public void canRegisterAddOnWithWebHookInScopes()
    {
        WebHookModuleBean webHookModuleBean = new WebHookModuleBeanBuilder().withEvent(EVENT_IN_SCOPES).build();

        connectAddonBean = newConnectAddonBean(connectAddonBean)
                .withModules("webhooks",webHookModuleBean)
                .withScopes(new HashSet<ScopeName>(Arrays.asList(ScopeName.ADMIN)))
                .build();

        beanToModuleRegistrar.registerDescriptorsForBeans(connectAddonBean);
    }

}
