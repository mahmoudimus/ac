package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
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

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

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
    private static final IFrameParams IFRAME_PARAMS = new IFrameParamsImpl();
    private static final IconBean ICON = IconBean.newIconBean().withUrl("moiImage").build();
    private static final ConditionalBean CONDITION_BEAN = new SingleConditionBeanBuilder().withCondition("unconditional").build();

    private final ConnectPageModuleBean defaultPageBean = createDefaultPageBean();
    private final PageToWebItemAndServletConverter defaultAdapter = createConverter(defaultPageBean, IFRAME_PARAMS);

    private PageToWebItemAndServletConverter emptyBeanAdapter;

    @Mock
    private ProductAccessor productAccessor;

    @Before
    public void init()
    {
        emptyBeanAdapter = createConverter(newPageBean().build(), null);
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
        assertThat(webItem(), hasProperty("location", is(LOCATION)));
    }

    @Test
    public void createsWebItemWithSameWeightAsPageBean()
    {
        assertThat(webItem(), hasProperty("weight", is(WEIGHT)));
    }

    @Test
    public void createsWebItemWithHostVersionOfPageBeanUrl()
    {
        assertThat(webItem(), hasProperty("url", is("/plugins/servlet/ac/" + PLUGIN_KEY + "/" + URL)));
    }

    @Test
    public void createsWebItemWithSameIconAsPageBean()
    {
        assertThat(webItem(), hasProperty("icon", is(ICON)));
    }

    @Test
    public void createsWebItemWithSameConditionAsPageBean()
    {
        assertThat(webItem(), hasProperty("conditions", contains(CONDITION_BEAN)));
    }

    @Test
    public void createsWebItemWithDefaultLocationWhenNotSpecified()
    {
        assertThat(emptyBeanAdapter.getWebItemBean().getLocation(), is(DEFAULT_LOCATION));
    }

    @Test
    public void createsWebItemWithDefaultWeightWhenNotSpecified()
    {
        assertThat(emptyBeanAdapter.getWebItemBean().getWeight(), is(DEFAULT_WEIGHT));
    }

    @Test
    public void createsServletBeanWithSameKeyAsPageBean()
    {
        assertThat(iFrameServlet(), hasProperty("linkBean", is(defaultPageBean)));
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

    @Test
    public void createsServletBeanWithExpectedIFrameParams()
    {
        assertThat(iFrameServlet(), hasProperty("iFrameParams", is(IFRAME_PARAMS)));
    }

    @Test
    public void createsServletBeanWithDefaultedIFrameParamsIfNotPresent()
    {
        assertThat(emptyBeanAdapter.getServletBean(), is(notNullValue()));
    }

    private PageInfo pageInfo()
    {
        return iFrameServlet().getPageInfo();
    }

    private WebItemModuleBean webItem()
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

    private PageToWebItemAndServletConverter createConverter(ConnectPageModuleBean pageBean, IFrameParams iFrameParams)
    {
        return new PageToWebItemAndServletConverter(pageBean, PLUGIN_KEY, DEFAULT_WEIGHT, DEFAULT_LOCATION,
                DECORATOR, TEMPLATE_SUFFIX, META_TAGS, CONDITION, iFrameParams);
    }

    private static ConnectPageModuleBean createDefaultPageBean()
    {
        return newPageBean()
                .withKey(PAGE_BEAN_KEY)
                .withLocation(LOCATION)
                .withName(NAME)
                .withUrl(URL)
                .withWeight(WEIGHT)
                .withIcon(ICON)
                .withConditions(CONDITION_BEAN)
                .build();
    }

}
