package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class PostInstallPageModuleProviderTest extends AbstractPageModuleProviderTest<PostInstallPageModuleProvider>
{
    @Override
    protected PostInstallPageModuleProvider createPageModuleProvider()
    {
        return new PostInstallPageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor);
    }

    @Test
    public void fetchesDefaultLocationFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor, never()).getPreferredGeneralSectionKey();
    }

    @Test
    public void fetchesDefaultWeightFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredGeneralWeight();
    }

}
