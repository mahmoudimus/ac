package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.plugin.capabilities.beans.NameToKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Matchers that operate on properties of the NameToKeyBean class
 */
public class NameToKeyBeanMatchers
{
    public static <T extends NameToKeyBean> ArgumentMatcher<T> hasKeyValue(String expectedValue)
    {
        return new NameToKeyBeanParamMatcher<T>("key", expectedValue)
        {
            @Override
            protected String getValue(NameToKeyBean capabilityBean)
            {
                return capabilityBean.getKey();
            }
        };
    }

    public static <T extends NameToKeyBean> ArgumentMatcher<T> hasNameValue(String expectedValue)
    {
        return new NameToKeyBeanParamMatcher<T>("name", expectedValue)
        {
            @Override
            protected String getValue(NameToKeyBean capabilityBean)
            {
                I18nProperty name = capabilityBean.getName();
                assertThat(name, is(notNullValue()));
                return name.getValue();
            }
        };
    }

    public static <T extends NameToKeyBean> ArgumentMatcher<T> hasNameI18KeyValue(String expectedValue)
    {
        return new NameToKeyBeanParamMatcher<T>("i18NameKey", expectedValue)
        {
            @Override
            protected String getValue(NameToKeyBean capabilityBean)
            {
                I18nProperty name = capabilityBean.getName();
                assertThat(name, is(notNullValue()));
                return name.getI18n();
            }
        };
    }

    private abstract static class NameToKeyBeanParamMatcher<T extends NameToKeyBean> extends CapabilityBeanParamMatcher<NameToKeyBean, T>
    {
        NameToKeyBeanParamMatcher(String name, Object expectedValue)
        {
            super(NameToKeyBean.class, name, expectedValue);
        }
    }

}
