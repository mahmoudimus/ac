package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.provider.GeneralPageModuleProvider;

import com.google.common.base.Objects;
import org.hamcrest.Description;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
public class GeneralPageModuleProviderTest extends AbstractPageModuleProviderTest<GeneralPageModuleProvider>
{
    @Override
    protected GeneralPageModuleProvider createPageModuleProvider()
    {
        return new GeneralPageModuleProvider(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, productAccessor);
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

    // handling explicitly as only one test on it
//    private ArgumentMatcher<IFrameServletBean> hasGeneralParamSet()
//    {
//        return new ArgumentMatcher<IFrameServletBean>()
//        {
//            @Override
//            public boolean matches(Object item)
//            {
//                assertThat(item, is(instanceOf(IFrameServletBean.class)));
//                IFrameServletBean iFrameServletBean = (IFrameServletBean) item;
//                return Objects.equal(iFrameServletBean.getiFrameParams().getAsMap().get("general"), "1");
//            }
//
//            @Override
//            public void describeTo(Description description)
//            {
//                description.appendText("IFrameServletBean with iframe param param general = 1");
//            }
//        };
//    }

}
