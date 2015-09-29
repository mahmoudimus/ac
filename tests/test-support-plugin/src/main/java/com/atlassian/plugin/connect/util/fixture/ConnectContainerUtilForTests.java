package com.atlassian.plugin.connect.util.fixture;

import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.mock;

public class ConnectContainerUtilForTests implements ConnectContainerUtil
{
    private final Map<Class<?>, Object> predefinedMocks = newHashMap();

    public ConnectContainerUtilForTests defineMock(Class<?> clazz, Object instance)
    {
        if (!clazz.isAssignableFrom(instance.getClass()))
        {
            throw new IllegalArgumentException(instance + " must implement " + clazz.getSimpleName());
        }
        predefinedMocks.put(clazz, instance);
        return this;
    }

    @Override
    public <T> T createBean(Class<T> clazz)
    {
        Constructor<?>[] constructors = clazz.getConstructors();
        Preconditions.checkState(constructors.length == 1);
        Constructor<T> constructor = (Constructor<T>) constructors[0];

        ArrayList<Object> mockArguments = newArrayList();

        for (Class<?> parameterClass : constructor.getParameterTypes())
        {
            Object mockedArgument = predefinedMocks.get(parameterClass);
            if (null == mockedArgument)
            {
                mockedArgument = mock(parameterClass);
            }
            mockArguments.add(mockedArgument);
        }

        try
        {
            return constructor.newInstance(mockArguments.toArray());
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Iterable<T> getBeansOfType(final Class<T> clazz)
    {
        @SuppressWarnings ("unchecked")
        Iterable<T> beansOfType = (Iterable<T>) Maps.filterKeys(predefinedMocks, new Predicate<Class<?>>()
        {
            @Override
            public boolean apply(final Class<?> input)
            {
                return input.getClass().equals(clazz);
            }
        }
        ).values();

        return beansOfType;
    }
}
