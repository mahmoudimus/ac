package com.atlassian.plugin.remotable.sisu;

/*
 *  Copyright 2012 The 99 Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Suppliers.alwaysFalse;

/**
 * A Guice {@code TypeListener} to hear annotated methods with lifecycle annotations.
 */
abstract class AbstractMethodTypeListener implements TypeListener
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The {@code java} package constants.
     */
    private static final String JAVA_PACKAGE = "java";

    /**
     * The lifecycle annotation to search on methods.
     */
    private final Class<? extends Annotation> annotationType;

    /**
     * Creates a new methods listener instance.
     *
     * @param annotationType the lifecycle annotation to search on methods.
     */
    public <A extends Annotation> AbstractMethodTypeListener( Class<A> annotationType )
    {
        this.annotationType = annotationType;
    }

    /**
     * {@inheritDoc}
     */
    public final <I> void hear( TypeLiteral<I> type, TypeEncounter<I> encounter )
    {
        hear( type.getRawType(), encounter );
    }

    /**
     * Allows traverse the input type hierarchy.
     *
     * @param type encountered by Guice.
     * @param encounter the injection context.
     */
    private <I> void hear( Class<? super I> type, TypeEncounter<I> encounter )
    {
        logger.debug("Encountering type: {}", type);

        if ( type == null || isProxy(type) || isJdkType(type))
        {
            return;
        }

        for ( Method method : type.getDeclaredMethods() )
        {
            if ( method.isAnnotationPresent( annotationType ) )
            {
                if ( method.getParameterTypes().length != 0 )
                {
                    encounter.addError( "Annotated methods with @%s must not accept any argument, found %s",
                            annotationType.getName(), method );
                }

                hear( method, encounter );
            }
        }

        hear( type.getSuperclass(), encounter );
    }

    @VisibleForTesting
    boolean isProxy(final Class<?> type)
    {
        return option(type)
                .map(new Function<Class<?>, String>()
                {
                    @Override
                    public String apply(Class<?> type)
                    {
                        return type.getSimpleName();
                    }
                })
                .fold(alwaysFalse(), new Function<String, Boolean>()
                {
                    @Override
                    public Boolean apply(String typeSimpleName)
                    {
                        final boolean isProxy = typeSimpleName.startsWith("$Proxy");
                        logger.debug("Type '{}' is {} a proxy", type, isProxy ? "" : "NOT");
                        return isProxy;
                    }
                });
    }

    @VisibleForTesting
    boolean isJdkType(Class<?> type)
    {
        return option(type)
                .map(new Function<Class<?>, Package>()
                {
                    @Override
                    public Package apply(Class<?> type)
                    {
                        return type.getPackage();
                    }
                })
                .map(new Function<Package, String>()
                {
                    @Override
                    public String apply(Package typePackage)
                    {
                        return typePackage.getName();
                    }
                })
                .fold(alwaysFalse(), new Function<String, Boolean>()
                {
                    @Override
                    public Boolean apply(String packageName)
                    {
                        return packageName.startsWith(JAVA_PACKAGE);
                    }
                });
    }

    /**
     * Returns the lifecycle annotation to search on methods.
     *
     * @return the lifecycle annotation to search on methods.
     */
    protected final Class<? extends Annotation> getAnnotationType()
    {
        return annotationType;
    }

    /**
     * Allows implementations to define the behavior when lifecycle annotation is found on the method.
     *
     * @param method encountered by this type handler.
     * @param encounter the injection context.
     */
    protected abstract <I> void hear( Method method, TypeEncounter<I> encounter );
}
