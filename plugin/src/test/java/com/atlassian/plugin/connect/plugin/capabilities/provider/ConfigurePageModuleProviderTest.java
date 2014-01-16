package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ConfigurePageModuleProviderTest extends AbstractPageModuleProviderTest<ConfigurePageModuleProvider>
{
    @Mock
    protected UserManager userManager;

    @Override
    protected ConfigurePageModuleProvider createPageModuleProvider()
    {
        return new ConfigurePageModuleProvider(iFrameRenderStrategyFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor, userManager);
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
