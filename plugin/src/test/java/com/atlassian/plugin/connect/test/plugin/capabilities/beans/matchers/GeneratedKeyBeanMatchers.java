package com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.modules.beans.GeneratedKeyBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Matchers that operate on properties of the GeneratedKeyBean class
 */
public class GeneratedKeyBeanMatchers
{
    public static <T extends GeneratedKeyBean> ArgumentMatcher<T> hasKeyValue(String expectedValue)
    {
        return new GeneratedKeyBeanParamMatcher<T>("key", expectedValue)
        {
            @Override
            protected String getValue(GeneratedKeyBean moduleBean)
            {
                return moduleBean.getKey();
            }
        };
    }

    public static <T extends GeneratedKeyBean> ArgumentMatcher<T> hasNameValue(String expectedValue)
    {
        return new GeneratedKeyBeanParamMatcher<T>("name", expectedValue)
        {
            @Override
            protected String getValue(GeneratedKeyBean moduleBean)
            {
                I18nProperty name = moduleBean.getName();
                assertThat(name, is(notNullValue()));
                return name.getValue();
            }
        };
    }

    public static <T extends GeneratedKeyBean> ArgumentMatcher<T> hasNameI18KeyValue(String expectedValue)
    {
        return new GeneratedKeyBeanParamMatcher<T>("i18NameKey", expectedValue)
        {
            @Override
            protected String getValue(GeneratedKeyBean moduleBean)
            {
                I18nProperty name = moduleBean.getName();
                assertThat(name, is(notNullValue()));
                return name.getI18n();
            }
        };
    }

    private abstract static class GeneratedKeyBeanParamMatcher<T extends GeneratedKeyBean> extends ModuleBeanParamMatcher<GeneratedKeyBean, T>
    {
        GeneratedKeyBeanParamMatcher(String name, Object expectedValue)
        {
            super(GeneratedKeyBean.class, name, expectedValue);
        }
    }

}
