package com.atlassian.plugin.connect.plugin.capabilities.testobjects;

import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class ConnectAutowireUtilForTests implements ConnectAutowireUtil
{
    private final Map<Class<?>, Object> predefinedMocks = Maps.newHashMap();

    public ConnectAutowireUtilForTests defineMock(Class<?> clazz, Object instance)
    {
        predefinedMocks.put(clazz, instance);
        return this;
    }

    @Override
    public <T> T createBean(Class<T> clazz)
    {
        Constructor<?>[] constructors = clazz.getConstructors();
        Preconditions.checkState(constructors.length == 1);
        Constructor<T> constructor = (Constructor<T>) constructors[0];

        ArrayList<Object> mockArguments = Lists.newArrayList();

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
}
