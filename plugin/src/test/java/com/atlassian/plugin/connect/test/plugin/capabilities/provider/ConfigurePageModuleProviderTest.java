package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConfigurePageModuleProvider;

import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class ConfigurePageModuleProviderTest extends AbstractPageModuleProviderTest<ConfigurePageModuleProvider>
{
    @Override
    protected ConfigurePageModuleProvider createPageModuleProvider()
    {
        return new ConfigurePageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor);
    }

    @Test
    public void fetchesDefaultLocationFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor, never()).getPreferredAdminSectionKey(); // kinda weird test
    }

    @Test
    public void fetchesDefaultWeightFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredAdminWeight();
    }

}
