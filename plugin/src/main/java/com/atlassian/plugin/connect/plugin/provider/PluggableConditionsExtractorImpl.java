package com.atlassian.plugin.connect.plugin.provider;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.condition.ConditionsProductProvider;
import com.atlassian.plugin.connect.spi.condition.ConditionsProvider;
import com.atlassian.plugin.connect.spi.condition.ConnectConditionModuleDescriptor;
import com.atlassian.plugin.connect.spi.condition.ConnectConditionModuleDescriptor.ConnectConditionClassProvider;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This component extracts context parameters based on local Connect conditions as well as conditions loaded from plug-ins.
 */
@Component
public class PluggableConditionsExtractorImpl extends AbstractPluggableModuleExtractor<ConnectConditionClassProvider> implements ConditionsProvider
{
    private final static Logger log = LoggerFactory.getLogger(PluggableConditionsExtractorImpl.class);

    private final ConditionsProductProvider productConditionsProvider;

    @Autowired
    public PluggableConditionsExtractorImpl(final ConditionsProductProvider productConditionsProvider, final PluginAccessor pluginAccessor)
    {
        super(pluginAccessor, ConnectConditionModuleDescriptor.class);
        this.productConditionsProvider = productConditionsProvider;
    }

    @Override
    public ConditionClassResolver getConditions() {
        ConditionClassResolver.Builder conditionClassBuilder = ConditionClassResolver.builder().with(productConditionsProvider.getConditions());
        for (ConnectConditionClassProvider provider : this.getModules()) {
            for (ConnectConditionModuleDescriptor.ConnectConditionDefinition conditionDefinition : provider.getConditionDefinitions()) {
                conditionClassBuilder.mapping(conditionDefinition.getName(), conditionDefinition.getClassName())
            }
            //conditionClassBuilder.mapping()
        }

        return conditionClassBuilder.build();
    }
}
