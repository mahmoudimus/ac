package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.google.inject.Binder;
import com.google.inject.Key;
import org.eclipse.sisu.binders.Wiring;

import java.util.HashSet;
import java.util.Set;

import static org.ops4j.peaberry.Peaberry.service;

public final class ComponentImportWiring implements Wiring
{
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Binder binder;

    public ComponentImportWiring(Binder binder)
    {
        this.binder = binder;
    }

    @Override
    public boolean wire(Key<?> key)
    {
        Class<?> beanClass = key.getTypeLiteral().getRawType();
        if (key.getAnnotation() instanceof ComponentImport)
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
            binder.bind(aClass).annotatedWith(ComponentImport.class).toProvider(service(aClass).single());
            classes.add(aClass);
        }
    }
}
