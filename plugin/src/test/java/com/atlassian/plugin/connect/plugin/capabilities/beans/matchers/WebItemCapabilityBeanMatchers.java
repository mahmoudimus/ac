package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import org.mockito.ArgumentMatcher;

public class WebItemCapabilityBeanMatchers extends NameToKeyBeanMatchers
{
    public static <T extends WebItemCapabilityBean> ArgumentMatcher<T> hasAddonKeyValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasKeyValue(expectedValue);
    }

    public static <T extends WebItemCapabilityBean> ArgumentMatcher<T> hasAddonNameValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasNameValue(expectedValue);
    }

    public static <T extends WebItemCapabilityBean> ArgumentMatcher<T> hasAddonNameI18KeyValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasNameI18KeyValue(expectedValue);
    }

    public static <T extends WebItemCapabilityBean> ArgumentMatcher<T> hasUrlValue(String expectedValue)
    {
        return new WebItemCapabilityBeanParamMatcher<T>("link", expectedValue)
        {
            @Override
            protected String getValue(WebItemCapabilityBean capabilityBean)
            {
                return capabilityBean.getLink();
            }
        };
    }

    public static <T extends WebItemCapabilityBean> ArgumentMatcher<T> hasWeightValue(int expectedValue)
    {
        return new WebItemCapabilityBeanParamMatcher<T>("weight", expectedValue)
        {
            @Override
            protected Integer getValue(WebItemCapabilityBean capabilityBean)
            {
                return capabilityBean.getWeight();
            }
        };
    }

    public static <T extends WebItemCapabilityBean> ArgumentMatcher<T> hasLocationValue(String expectedValue)
    {
        return new WebItemCapabilityBeanParamMatcher<T>("location", expectedValue)
        {
            @Override
            protected String getValue(WebItemCapabilityBean capabilityBean)
            {
                return capabilityBean.getLocation();
            }
        };
    }

    private abstract static class WebItemCapabilityBeanParamMatcher<T extends WebItemCapabilityBean>
            extends CapabilityBeanParamMatcher<WebItemCapabilityBean, T>
    {
        WebItemCapabilityBeanParamMatcher(String name, Object expectedValue)
        {
            super(WebItemCapabilityBean.class, name, expectedValue);
        }
    }
}
