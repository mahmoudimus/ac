package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.confluence.provider.AbstractPageModuleProviderTest;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class ProfilePageModuleProviderTest extends AbstractPageModuleProviderTest<ProfilePageModuleProvider>
{
    @Mock
    private ConnectJsonSchemaValidator schemaValidator;

    @Override
    protected ProfilePageModuleProvider createPageModuleProvider()
    {
        return new ProfilePageModuleProvider(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsFactory, schemaValidator, productAccessor);
    }

    @Test
    public void fetchesDefaultLocationFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredProfileSectionKey();
    }

    @Test
    public void fetchesDefaultWeightFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredProfileWeight();
    }
}
