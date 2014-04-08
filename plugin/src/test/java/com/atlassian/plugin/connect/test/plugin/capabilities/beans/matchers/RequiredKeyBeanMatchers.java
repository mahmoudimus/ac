package com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Matchers that operate on properties of the GeneratedKeyBean class
 */
public class RequiredKeyBeanMatchers
{
    public static <T extends RequiredKeyBean> ArgumentMatcher<T> hasKeyValue(String expectedValue)
    {
        return new RequiredKeyBeanParamMatcher<T>("key", expectedValue)
        {
            @Override
            protected String getValue(RequiredKeyBean moduleBean)
            {
                return moduleBean.getRawKey();
            }
        };
    }

    public static <T extends RequiredKeyBean> ArgumentMatcher<T> hasNameValue(String expectedValue)
    {
        return new RequiredKeyBeanParamMatcher<T>("name", expectedValue)
        {
            @Override
            protected String getValue(RequiredKeyBean moduleBean)
            {
                I18nProperty name = moduleBean.getName();
                assertThat(name, is(notNullValue()));
                return name.getValue();
            }
        };
    }

    public static <T extends RequiredKeyBean> ArgumentMatcher<T> hasNameI18KeyValue(String expectedValue)
    {
        return new RequiredKeyBeanParamMatcher<T>("i18NameKey", expectedValue)
        {
            @Override
            protected String getValue(RequiredKeyBean moduleBean)
            {
                I18nProperty name = moduleBean.getName();
                assertThat(name, is(notNullValue()));
                return name.getI18n();
            }
        };
    }

    private abstract static class RequiredKeyBeanParamMatcher<T extends RequiredKeyBean> extends ModuleBeanParamMatcher<RequiredKeyBean, T>
    {
        RequiredKeyBeanParamMatcher(String name, Object expectedValue)
        {
            super(RequiredKeyBean.class, name, expectedValue);
        }
    }

}
