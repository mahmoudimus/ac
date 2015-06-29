package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class PostUpdatePageModuleProviderTest extends AbstractPageModuleProviderTest<PostUpdatePageModuleProvider>
{
    @Override
    protected PostUpdatePageModuleProvider createPageModuleProvider()
    {
        return new PostUpdatePageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
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
