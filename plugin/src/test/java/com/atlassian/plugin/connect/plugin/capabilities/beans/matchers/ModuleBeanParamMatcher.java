package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.google.common.base.Objects;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * A hamcrest ArgumentMatcher for matching against properties of ModuleBeans
 * @param <M> The type of module bean that is required for the matching methods on this class
 * @param <T> The type of module bean the test is running against. Note this is typically a subtype of M
 */
public abstract class ModuleBeanParamMatcher<M extends ModuleBean, T extends M> extends ArgumentMatcher<T>
{
    private final String name;
    private final Object expectedValue;
    private final Class<M> beanClass;

    public ModuleBeanParamMatcher(Class<M> beanClass, String name, Object expectedValue)
    {
        this.name = checkNotNull(name);
        this.expectedValue = checkNotNull(expectedValue);
        this.beanClass = checkNotNull(beanClass);
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(beanClass)));
        T moduleBean = (T) argument;
        return matchesOnModuleBean(moduleBean, expectedValue);
    }

    protected boolean matchesOnModuleBean(T moduleBean, Object expectedValue)
    {
        return Objects.equal(getValue(moduleBean), expectedValue);
    }

    protected abstract Object getValue(T moduleBean);

    @Override
    public void describeTo(Description description)
    {
        description.appendText("ModuleBean with param ");
        description.appendValue(name);
        description.appendText(" = ");
        description.appendValue(expectedValue);
    }
}
