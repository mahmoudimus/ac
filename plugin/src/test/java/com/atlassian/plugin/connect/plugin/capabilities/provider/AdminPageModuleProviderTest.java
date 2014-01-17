package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

public class AdminPageModuleProviderTest extends AbstractPageModuleProviderTest<AdminPageModuleProvider>
{
    @Mock protected UserManager userManager;

    @Override
    protected AdminPageModuleProvider createPageModuleProvider()
    {
        return new AdminPageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor, userManager);
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
