package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultWebItemModuleProviderTest {

    @Mock
    WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock
    IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    @Mock
    IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private DefaultWebItemModuleProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new DefaultWebItemModuleProvider(webItemModuleDescriptorFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry);
    }

    @Test
    public void testWebItemLinkContainsWebItemSourceQueryParamWithKey() throws Exception {

        String addonKey = "addonKey";
        String webitemKey = "webitemKey";

        ConnectAddonBean addonBean = mock(ConnectAddonBean.class);
        when(addonBean.getKey()).thenReturn(addonKey);
        when(addonBean.getBaseUrl()).thenReturn("http://abc");

        ConnectModuleProviderContext connectModuleProviderContext = mock(ConnectModuleProviderContext.class);
        when(connectModuleProviderContext.getConnectAddonBean()).thenReturn(addonBean);

        WebItemModuleBean webItemModuleBean = WebItemModuleBean.newWebItemBean().withContext(AddOnUrlContext.page).withKey(webitemKey).build();

        provider.provideModules(connectModuleProviderContext, null, null, Arrays.asList(webItemModuleBean));

        String key = webItemModuleBean.getKey(addonBean);

        verify(webItemModuleDescriptorFactory).createModuleDescriptor(
                Matchers.<ConnectModuleProviderContext>anyObject(),
                Matchers.<Plugin>anyObject(),
                argThat(webItemHasUrlWithCorrectQueryParam(key)));
    }

    private Matcher<WebItemModuleBean> webItemHasUrlWithCorrectQueryParam(final String value) {
        return new TypeSafeMatcher<WebItemModuleBean>() {
            @Override
            protected boolean matchesSafely(WebItemModuleBean webItemModuleBean) {
                return webItemModuleBean.getUrl().contains(DefaultWebItemModuleProvider.WEB_ITEM_SOURCE_QUERY_PARAM + "=" + value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Webitem with url containing " + DefaultWebItemModuleProvider.WEB_ITEM_SOURCE_QUERY_PARAM + "=" + value);
            }
        };
    }
}