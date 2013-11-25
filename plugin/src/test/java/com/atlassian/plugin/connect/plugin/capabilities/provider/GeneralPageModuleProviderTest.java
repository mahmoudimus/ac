package com.atlassian.plugin.connect.plugin.capabilities.provider;

import org.junit.Test;

import static org.mockito.Mockito.verify;

public class GeneralPageModuleProviderTest extends AbstractPageModuleProviderTest<GeneralPageModuleProvider>
{
    @Override
    protected GeneralPageModuleProvider createPageModuleProvider()
    {
        return new GeneralPageModuleProvider(webItemModuleDescriptorFactory, servletDescriptorFactory, productAccessor);
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
