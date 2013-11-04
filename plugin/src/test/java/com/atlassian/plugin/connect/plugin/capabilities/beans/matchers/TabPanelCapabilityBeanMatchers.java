package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import org.mockito.ArgumentMatcher;

/**
 * Matchers that operate on properties of the AbstractConnectTabPanelCapabilityBean class
 */
public class TabPanelCapabilityBeanMatchers extends NameToKeyBeanMatchers
{
    public static <T extends AbstractConnectTabPanelCapabilityBean> ArgumentMatcher<T> hasAddonKeyValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasKeyValue(expectedValue);
    }

    public static <T extends AbstractConnectTabPanelCapabilityBean> ArgumentMatcher<T> hasAddonNameValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasNameValue(expectedValue);
    }

    public static <T extends AbstractConnectTabPanelCapabilityBean> ArgumentMatcher<T> hasAddonNameI18KeyValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasNameI18KeyValue(expectedValue);
    }

    public static <T extends AbstractConnectTabPanelCapabilityBean> ArgumentMatcher<T> hasUrlValue(String expectedValue)
    {
        return new TabPanelCapabilityBeanParamMatcher<T>("url", expectedValue)
        {
            @Override
            protected String getValue(AbstractConnectTabPanelCapabilityBean capabilityBean)
            {
                return capabilityBean.getUrl();
            }
        };
    }

    public static <T extends AbstractConnectTabPanelCapabilityBean> ArgumentMatcher<T> hasWeightValue(int expectedValue)
    {
        return new TabPanelCapabilityBeanParamMatcher<T>("weight", expectedValue)
        {
            @Override
            protected Integer getValue(AbstractConnectTabPanelCapabilityBean capabilityBean)
            {
                return capabilityBean.getWeight();
            }
        };
    }

    private abstract static class TabPanelCapabilityBeanParamMatcher<T extends AbstractConnectTabPanelCapabilityBean>
            extends CapabilityBeanParamMatcher<AbstractConnectTabPanelCapabilityBean, T>
    {
        TabPanelCapabilityBeanParamMatcher(String name, Object expectedValue)
        {
            super(AbstractConnectTabPanelCapabilityBean.class, name, expectedValue);
        }
    }
}
