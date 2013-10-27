package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.google.common.base.Objects;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * A hamcrest ArgumentMatcher for matching against properties of CapabilityBeans
 * @param <M> The type of capability bean that is required for the matching methods on this class
 * @param <T> The type of capability bean the test is running against. Note this is typically a subtype of M
 */
public abstract class CapabilityBeanParamMatcher<M extends CapabilityBean, T extends M> extends ArgumentMatcher<T>
{
    private final String name;
    private final Object expectedValue;
    private final Class<M> beanClass;

    public CapabilityBeanParamMatcher(Class<M> beanClass, String name, Object expectedValue)
    {
        this.name = checkNotNull(name);
        this.expectedValue = checkNotNull(expectedValue);
        this.beanClass = checkNotNull(beanClass);
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(beanClass)));
        T capabilityBean = (T) argument;
        return matchesOnCapabilityBean(capabilityBean, expectedValue);
    }

    protected boolean matchesOnCapabilityBean(T capabilityBean, Object expectedValue)
    {
        return Objects.equal(getValue(capabilityBean), expectedValue);
    }

    protected abstract Object getValue(T capabilityBean);

    @Override
    public void describeTo(Description description)
    {
        description.appendText("CapabilityBean with param ");
        description.appendValue(name);
        description.appendText(" = ");
        description.appendValue(expectedValue);
    }
}
