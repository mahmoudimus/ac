package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.AdminPageModuleProvider;
import com.atlassian.sal.api.user.UserManager;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
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
