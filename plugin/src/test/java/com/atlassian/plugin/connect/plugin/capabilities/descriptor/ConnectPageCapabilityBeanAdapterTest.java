package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConnectPageCapabilityBeanAdapterTest
{
    private static final String PLUGIN_KEY = "FOO";
    private static final String PAGE_BEAN_KEY = "KEY";
    private static final String LOCATION = "location";
    private static final I18nProperty NAME = new I18nProperty("name", "i18key");
    private static final String URL = "foo?blah";
    private static final int WEIGHT = 10;

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

    private WebItemCapabilityBean webItem()
    {
        return defaultAdapter().getWebItemBean();
    }

    private ConnectPageCapabilityBeanAdapter defaultAdapter()
    {
        return new ConnectPageCapabilityBeanAdapter(defaultPageBean(), PLUGIN_KEY);
    }

    private ConnectPageCapabilityBean defaultPageBean()
    {
        return ConnectPageCapabilityBean.newPageBean()
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
