package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import org.mockito.ArgumentMatcher;

/**
 * Matchers that operate on properties of the WebItemModuleBean class
 */

public class WebItemModuleBeanMatchers extends NameToKeyBeanMatchers
{
    public static <T extends WebItemModuleBean> ArgumentMatcher<T> hasAddonKeyValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasKeyValue(expectedValue);
    }

    public static <T extends WebItemModuleBean> ArgumentMatcher<T> hasAddonNameValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasNameValue(expectedValue);
    }

    public static <T extends WebItemModuleBean> ArgumentMatcher<T> hasAddonNameI18KeyValue(String expectedValue)
    {
        return NameToKeyBeanMatchers.hasNameI18KeyValue(expectedValue);
    }

    public static <T extends WebItemModuleBean> ArgumentMatcher<T> hasUrlValue(String expectedValue)
    {
        return new WebItemModuleBeanParamMatcher<T>("link", expectedValue)
        {
            @Override
            protected String getValue(WebItemModuleBean moduleBean)
            {
                return moduleBean.getUrl();
            }
        };
    }

    public static <T extends WebItemModuleBean> ArgumentMatcher<T> hasWeightValue(int expectedValue)
    {
        return new WebItemModuleBeanParamMatcher<T>("weight", expectedValue)
        {
            @Override
            protected Integer getValue(WebItemModuleBean moduleBean)
            {
                return moduleBean.getWeight();
            }
        };
    }

    public static <T extends WebItemModuleBean> ArgumentMatcher<T> hasLocationValue(String expectedValue)
    {
        return new WebItemModuleBeanParamMatcher<T>("location", expectedValue)
        {
            @Override
            protected String getValue(WebItemModuleBean moduleBean)
            {
                return moduleBean.getLocation();
            }
        };
    }

    private abstract static class WebItemModuleBeanParamMatcher<T extends WebItemModuleBean>
            extends ModuleBeanParamMatcher<WebItemModuleBean, T>
    {
        WebItemModuleBeanParamMatcher(String name, Object expectedValue)
        {
            super(WebItemModuleBean.class, name, expectedValue);
        }
    }
}
