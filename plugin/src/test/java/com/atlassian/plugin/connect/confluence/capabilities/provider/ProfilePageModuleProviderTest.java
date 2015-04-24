package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.confluence.capabilities.provider.ProfilePageModuleProvider;
import com.atlassian.plugin.connect.test.plugin.capabilities.provider.AbstractPageModuleProviderTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class ProfilePageModuleProviderTest extends AbstractPageModuleProviderTest<ProfilePageModuleProvider>
{
    @Override
    protected ProfilePageModuleProvider createPageModuleProvider()
    {
        return new ProfilePageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor);
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
