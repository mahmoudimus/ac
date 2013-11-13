package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean.newPageBean;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageToWebItemAndServletConverterTest
{
    private static final String PLUGIN_KEY = "FOO";
    private static final String PAGE_BEAN_KEY = "KEY";
    private static final String LOCATION = "location";
    private static final I18nProperty NAME = new I18nProperty("name", "i18key");
    private static final String URL = "foo?blah";
    private static final int WEIGHT = 10;

    private static final String DECORATOR = "deco";
    private static final String TEMPLATE_SUFFIX = "SUFFICKS";

    private static final Condition CONDITION = new AlwaysDisplayCondition();
    private static final Map<String, String> META_TAGS = ImmutableMap.of("foo", "bar");
    private static final String DEFAULT_LOCATION = "defaultLocation";
    private static final int DEFAULT_WEIGHT = 123;

    private final ConnectPageCapabilityBean defaultPageBean = createDefaultPageBean();
    private final PageToWebItemAndServletConverter defaultAdapter = createAdapter(defaultPageBean);
    private PageToWebItemAndServletConverter emptyBeanAdapter;

    @Mock
    private ProductAccessor productAccessor;

    @Before
    public void init()
    {
        when(productAccessor.getPreferredGeneralSectionKey()).thenReturn(DEFAULT_LOCATION);
        when(productAccessor.getPreferredGeneralWeight()).thenReturn(DEFAULT_WEIGHT);
        emptyBeanAdapter = createAdapter(newPageBean().build());
    }

    @Test
    public void createsWebItemWithSameKeyAsPageBean()
    {
        assertThat(webItem(), hasProperty("key", is(PAGE_BEAN_KEY)));
    }

    @Test
    public void createsWebItemWithSameNameAsPageBean()
    {
        assertThat(webItem(), hasProperty("name", is(NAME)));
    }

    @Test
    public void createsWebItemWithSameLocationAsPageBean()
    {
        assertThat(webItem(), hasProperty("location", is(LOCATION))); // TODO: This is probably not true. Think needs a prefix
    }

    @Test
    public void createsWebItemWithSameWeightAsPageBean()
    {
        assertThat(webItem(), hasProperty("weight", is(WEIGHT)));
    }

    @Test
    public void createsWebItemWithHostVersionOfPageBeanUrl()
    {
        assertThat(webItem(), hasProperty("link", is("/plugins/servlet/ac/" + PLUGIN_KEY + "/" + URL)));
    }

    @Test
    public void fetchesDefaultLocationFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredGeneralSectionKey();
    }

    @Test
    public void createsWebItemWithDefaultLocationWhenNotSpecified()
    {
        assertThat(emptyBeanAdapter.getWebItemBean().getLocation(), is(DEFAULT_LOCATION));
    }

    @Test
    public void fetchesDefaultWeightFromProductAccessorWhenNotSpecified()
    {
        verify(productAccessor).getPreferredGeneralWeight();
    }

    @Test
    public void createsWebItemWithDefaultWeightWhenNotSpecified()
    {
        assertThat(emptyBeanAdapter.getWebItemBean().getWeight(), is(DEFAULT_WEIGHT));
    }


    @Test
    public void createsServletBeanWithSameKeyAsPageBean()
    {
        assertThat(iFrameServlet(), hasProperty("linkBean", is(defaultPageBean))); // TODO: Or should that be the web item?
    }

    @Test
    public void createsServletBeanWithExpectedUrlTemplatePair()
    {
        assertThat(urlTemplatePair(), is(new AddonUrlTemplatePair(URL, PLUGIN_KEY)));
    }

    @Test
    public void createsServletBeanWithExpectedDecorator()
    {
        assertThat(pageInfo(), hasProperty("decorator", is(DECORATOR)));
    }

    @Test
    public void createsServletBeanWithExpectedTemplateSuffix()
    {
        assertThat(pageInfo(), hasProperty("templateSuffix", is(TEMPLATE_SUFFIX)));
    }

    @Test
    public void createsServletBeanWithExpectedTitle()
    {
        assertThat(pageInfo(), hasProperty("title", is(NAME.getValue())));
    }

    @Test
    public void createsServletBeanWithExpectedCondition()
    {
        assertThat(pageInfo(), hasProperty("condition", is(CONDITION)));
    }

    @Test
    public void createsServletBeanWithExpectedMetaTags()
    {
        assertThat(pageInfo(), hasProperty("metaTagsContent", is(META_TAGS)));
    }

    @Test
    public void createsServletBeanWithDefaultedConditionWhenNotPresent()
    {
        assertThat(emptyBeanAdapter.getServletBean().getPageInfo().getCondition(), is(instanceOf(AlwaysDisplayCondition.class)));
    }

    private PageInfo pageInfo()
    {
        return iFrameServlet().getPageInfo();
    }

    private WebItemCapabilityBean webItem()
    {
        return defaultAdapter.getWebItemBean();
    }


    private AddonUrlTemplatePair urlTemplatePair()
    {
        return iFrameServlet().getUrlTemplatePair();
    }

    private IFrameServletBean iFrameServlet()
    {
        return defaultAdapter.getServletBean();
    }

    private PageToWebItemAndServletConverter createAdapter(ConnectPageCapabilityBean pageBean)
    {
        return new PageToWebItemAndServletConverter(pageBean, PLUGIN_KEY, productAccessor,
                DECORATOR, TEMPLATE_SUFFIX, META_TAGS, CONDITION);
    }

    private static ConnectPageCapabilityBean createDefaultPageBean()
    {
        return newPageBean()
                .withKey(PAGE_BEAN_KEY)
                .withLocation(LOCATION)
                .withName(NAME)
                .withUrl(URL)
                .withWeight(WEIGHT)
//                .withParam()
//                .withConditions()
//                .withIcon()
                .build();
    }

}
