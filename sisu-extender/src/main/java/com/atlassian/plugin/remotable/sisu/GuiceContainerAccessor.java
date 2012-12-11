package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.module.ContainerAccessor;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

/**
* TODO: Document this class / interface here
*
* @since v5.2
*/
public class GuiceContainerAccessor implements ContainerAccessor
{
    private final Injector injector;

    public GuiceContainerAccessor(Injector injector)
    {
        this.injector = injector;
    }

    @Override
    public <T> T createBean(Class<T> tClass)
    {
        return injector.getInstance(tClass);
    }

    @Override
    public <T> T injectBean(T bean)
    {
        injector.injectMembers(bean);
        return bean;
    }

    @Override
    public <T> T getBean(String id)
    {
        return (T) injector.getInstance(Key.get(String.class, Names.named("annotation")));
    }

    @Override
    public <T> Collection<T> getBeansOfType(final Class<T> tClass)
    {
        return transform(filter(injector.getAllBindings().entrySet(), new Predicate<Map.Entry<Key<?>, Binding<?>>>()
        {
            @Override
            public boolean apply(@Nullable Map.Entry<Key<?>, Binding<?>> input)
            {
                return tClass.isAssignableFrom(input.getKey().getTypeLiteral().getRawType());
            }
        }), new Function<Map.Entry<Key<?>, Binding<?>>, T>()
        {
            @Override
            public T apply(@Nullable Map.Entry<Key<?>, Binding<?>> input)
            {
                return (T) input.getValue().getProvider().get();
            }
        });
    }
}
