package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.AdminPageModuleProvider;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class AdminPageModuleProviderTest extends AbstractPageModuleProviderTest<AdminPageModuleProvider>
{
    @Override
    protected AdminPageModuleProvider createPageModuleProvider()
    {
        return new AdminPageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor);
    }

    @Test
    public void fetchesDefaultLocationFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredAdminSectionKey();
    }

    @Test
    public void fetchesDefaultWeightFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredAdminWeight();
    }

}
