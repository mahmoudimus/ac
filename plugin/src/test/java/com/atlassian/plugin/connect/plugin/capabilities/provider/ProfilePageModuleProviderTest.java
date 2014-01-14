package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.nested.IFrameServletBean;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class ProfilePageModuleProviderTest extends AbstractPageModuleProviderTest<ProfilePageModuleProvider>
{
    @Override
    protected ProfilePageModuleProvider createPageModuleProvider()
    {
        return new ProfilePageModuleProvider(webItemModuleDescriptorFactory, servletDescriptorFactory, productAccessor);
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

    @Test
    public void callsServletDescriptorFactory()
    {
        verify(servletDescriptorFactory()).createIFrameServletDescriptor(eq(plugin), any(IFrameServletBean.class));
    }
}
