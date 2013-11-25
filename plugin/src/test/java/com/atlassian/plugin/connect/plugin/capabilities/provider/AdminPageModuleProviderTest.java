package com.atlassian.plugin.connect.plugin.capabilities.provider;

import org.junit.Test;

import static org.mockito.Mockito.verify;

public class AdminPageModuleProviderTest extends AbstractPageModuleProviderTest<AdminPageModuleProvider>
{
    @Override
    protected AdminPageModuleProvider createPageModuleProvider()
    {
        return new AdminPageModuleProvider(webItemModuleDescriptorFactory, servletDescriptorFactory, productAccessor);
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
