package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.remotable.api.annotation.ServiceReference;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.DefaultElementVisitor;
import org.eclipse.sisu.binders.Wiring;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import static org.ops4j.peaberry.Peaberry.service;

public final class ServiceReferenceWiring implements Wiring
{
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Binder binder;

    public ServiceReferenceWiring(Binder binder)
    {
        this.binder = binder;
    }

    @Override
    public boolean wire(Key<?> key)
    {
        Class<?> beanClass = key.getTypeLiteral().getRawType();
        if (key.getAnnotation() instanceof ServiceReference)
        {
            addOsgiServiceFor(beanClass);
            return true;
        }
        return false;
    }

    private <T> void addOsgiServiceFor(Class<T> aClass)
    {
        if (!classes.contains(aClass))
        {
            binder.bind(aClass).annotatedWith(ServiceReference.class).toProvider(service(aClass).single());
            classes.add(aClass);
        }
    }
}
