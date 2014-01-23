package com.atlassian.plugin.connect.test.plugin.capabilities;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebHookModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.WebHookScopeService;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.AutowireWithConnectPluginDecorator;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
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
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BeanToModuleRegistrarTest
{
    private BeanToModuleRegistrar beanToModuleRegistrar;

    @Mock private DynamicDescriptorRegistration dynamicDescriptorRegistration;
    @Mock private PluginRetrievalService pluginRetrievalService;
    @Mock private ProductAccessor productAccessor;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private WebHookScopeService webHookScopeService;

    @Mock private ConnectAddonBean connectAddonBean;
    @Mock private LifecycleBean lifecycleBean;
    @Mock private OsgiPlugin plugin;
    @Mock private Bundle bundle;
    @Mock private AutowireWithConnectPluginDecorator theConnectPlugin;
    @Mock private ContainerAccessor containerAccessor;
    @Mock private ModuleList moduleList;

    private static final String EVENT_IN_SCOPES = "event in scopes";

    @Before
    public void beforeEachTest()
    {
        when(plugin.getBundle()).thenReturn(bundle);
        when(plugin.getKey()).thenReturn("a plugin key");
        when(connectAddonBean.getLifecycle()).thenReturn(lifecycleBean);
        when(applicationProperties.getDisplayName()).thenReturn("JIRA");
        when(pluginRetrievalService.getPlugin()).thenReturn(theConnectPlugin);
        when(theConnectPlugin.getContainerAccessor()).thenReturn(containerAccessor);
        when(webHookScopeService.getRequiredScope(EVENT_IN_SCOPES)).thenReturn(ScopeName.ADMIN);
        when(connectAddonBean.getModules()).thenReturn(moduleList);
        when(moduleList.getWebhooks()).thenReturn(Collections.<WebHookModuleBean>emptyList());
        beanToModuleRegistrar = new BeanToModuleRegistrar(dynamicDescriptorRegistration, pluginRetrievalService, productAccessor, applicationProperties, webHookScopeService);
    }

    @Test
    public void canRegisterAddOnWithNoWebHooks()
    {
        when(connectAddonBean.getModules()).thenReturn(new ModuleList());
        beanToModuleRegistrar.registerDescriptorsForBeans(plugin, connectAddonBean);
    }

    @Test
    public void canRegisterAddOnWithWebHookInScopes()
    {
        WebHookModuleBean webHookModuleBean = new WebHookModuleBeanBuilder().withEvent(EVENT_IN_SCOPES).build();
        when(moduleList.getWebhooks()).thenReturn(Arrays.asList(webHookModuleBean));
        when(connectAddonBean.getScopes()).thenReturn(new HashSet<ScopeName>(Arrays.asList(ScopeName.ADMIN)));
        beanToModuleRegistrar.registerDescriptorsForBeans(plugin, connectAddonBean);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void cannotRegisterAddOnWithWebHooksOutsideOfScopes()
    {
        WebHookModuleBean webHookModuleBean = new WebHookModuleBeanBuilder().withEvent(EVENT_IN_SCOPES).build();
        when(moduleList.getWebhooks()).thenReturn(Arrays.asList(webHookModuleBean));
        when(connectAddonBean.getScopes()).thenReturn(new HashSet<ScopeName>(Arrays.asList(ScopeName.READ)));
        beanToModuleRegistrar.registerDescriptorsForBeans(plugin, connectAddonBean);
    }
}
