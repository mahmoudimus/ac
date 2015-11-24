package com.atlassian.plugin.connect.plugin.web.page;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.Condition;

import java.util.Collections;

public abstract class AbstractAdminPageModuleProvider extends AbstractConnectCorePageModuleProvider
{

    public static final String ATL_ADMIN_DECORATOR = "atl.admin";

    private final ProductAccessor productAccessor;

    public AbstractAdminPageModuleProvider(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ConditionClassAccessor conditionClassAccessor,
            ConnectJsonSchemaValidator schemaValidator,
            ProductAccessor productAccessor)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, conditionClassAccessor, schemaValidator);
        this.productAccessor = productAccessor;
    }

    @Override
    protected String getDecorator()
    {
        return ATL_ADMIN_DECORATOR;
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredAdminSectionKey();
    }

    @Override
    protected int getDefaultWeight()
    {
        return productAccessor.getPreferredAdminWeight();
    }

    @Override
    protected Iterable<Class<? extends Condition>> getConditionClasses()
    {
        return Collections.<Class<? extends Condition>>singletonList(UserIsAdminCondition.class);
    }

    @Override
    protected boolean needsEscaping()
    {
        return productAccessor.needsAdminPageNameEscaping();
    }
}
