package com.atlassian.plugin.connect.spi.capabilities.provider;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractConnectPageModuleProviderTest
{

    private static final String VALID_CONDITION = "user_is_logged_in";
    private static final String INVALID_CONDITION = "user_is_project_admin";

    private AbstractConnectPageModuleProvider provider;

    @Mock
    private PluginRetrievalService pluginRetrievalService;

    @Mock
    private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    @Mock
    private IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    @Mock
    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Mock
    private PageConditionsFactory pageConditionsFactory;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp()
    {
        provider = new AbstractConnectPageModuleProviderForTesting(pluginRetrievalService, iFrameRenderStrategyBuilderFactory,
                iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory, pageConditionsFactory);
        when(pageConditionsFactory.getConditionNames()).thenReturn(Collections.singleton(VALID_CONDITION));
    }

    @Test
    public void validBuiltInConditionPasses() throws ConnectModuleValidationException
    {
        SingleConditionBean condition = newCondition(VALID_CONDITION);
        provider.validateConditions(Collections.singletonList(newPage(condition)));
    }

    @Test
    public void validRemoteConditionPasses() throws ConnectModuleValidationException
    {
        SingleConditionBean condition = newCondition("/remote-condition");
        provider.validateConditions(Collections.singletonList(newPage(condition)));
    }

    @Test
    public void validNestedBuiltInConditionPasses() throws ConnectModuleValidationException
    {
        CompositeConditionBean condition = newCompositeConditionBean().withConditions(newCondition(VALID_CONDITION))
                .withType(CompositeConditionType.AND).build();
        provider.validateConditions(Collections.singletonList(newPage(condition)));
    }

    @Test
    public void validNestedRemoteConditionPasses() throws ConnectModuleValidationException
    {
        CompositeConditionBean condition = newCompositeConditionBean().withConditions(newCondition("/remote-condition"))
                .withType(CompositeConditionType.AND).build();
        provider.validateConditions(Collections.singletonList(newPage(condition)));
    }

    @Test
    public void invalidConditionFails() throws ConnectModuleValidationException
    {
        expectValidationException(INVALID_CONDITION);
        SingleConditionBean condition = newCondition(INVALID_CONDITION);
        provider.validateConditions(Collections.singletonList(newPage(condition)));
    }

    @Test
    public void invalidNestedConditionFails() throws ConnectModuleValidationException
    {
        expectValidationException(INVALID_CONDITION);
        CompositeConditionBean condition = newCompositeConditionBean().withConditions(newCondition(INVALID_CONDITION))
                .withType(CompositeConditionType.AND).build();
        provider.validateConditions(Collections.singletonList(newPage(condition)));
    }

    private SingleConditionBean newCondition(String condition)
    {
        return SingleConditionBean.newSingleConditionBean().withCondition(condition).build();
    }

    private ConnectPageModuleBean newPage(ConditionalBean condition)
    {
        return ConnectPageModuleBean.newPageBean().withConditions(condition).build();
    }

    private void expectValidationException(String conditionName)
    {
        expectedException.expect(ConnectModuleValidationException.class);
        expectedException.expectMessage(String.format("The add-on includes a Page Module with an unsupported condition (%s)", conditionName));
    }

    private static class AbstractConnectPageModuleProviderForTesting extends AbstractConnectPageModuleProvider
    {
        public AbstractConnectPageModuleProviderForTesting(PluginRetrievalService pluginRetrievalService,
                IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                PageConditionsFactory pageConditionsFactory)
        {
            super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                    webItemModuleDescriptorFactory, pageConditionsFactory);
        }

        @Override
        public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
        {
            return new ConnectModuleMeta<ConnectPageModuleBean>(null, ConnectPageModuleBean.class) {};
        }

        @Override
        protected String getDecorator()
        {
            return null;
        }

        @Override
        protected String getDefaultSection()
        {
            return null;
        }

        @Override
        protected int getDefaultWeight()
        {
            return 0;
        }
    }
}
