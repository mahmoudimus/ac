package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class GeneralPageModuleProviderTest extends AbstractPageModuleProviderTest<GeneralPageModuleProvider>
{
    @Override
    protected GeneralPageModuleProvider createPageModuleProvider()
    {
        return new GeneralPageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsValidator, productAccessor);
    }

    @Test
    public void fetchesDefaultLocationFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredGeneralSectionKey();
    }

    @Test
    public void fetchesDefaultWeightFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredGeneralWeight();
    }

}
