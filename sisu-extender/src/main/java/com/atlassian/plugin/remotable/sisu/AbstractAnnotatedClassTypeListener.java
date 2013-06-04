package com.atlassian.plugin.remotable.sisu;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractAnnotatedClassTypeListener<A extends Annotation> implements TypeListener
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The lifecycle annotation to search on methods.
     */
    private final Class<A> annotationType;

    protected AbstractAnnotatedClassTypeListener(Class<A> annotationType)
    {
        this.annotationType = checkNotNull(annotationType);
    }

    @Override
    public final <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
    {
        final Class<?> clazz = type.getRawType();
        if (clazz.isAnnotationPresent(annotationType))
        {
            hear(type, encounter, clazz.getAnnotation(annotationType));
        }
    }

    protected abstract <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter, A annotation);
}
