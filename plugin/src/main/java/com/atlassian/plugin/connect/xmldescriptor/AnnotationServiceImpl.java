package com.atlassian.plugin.connect.xmldescriptor;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@ExportAsDevService
@Named("annotationService")
public class AnnotationServiceImpl implements AnnotationService
{
    @Override
    public Class loadClass(String className) throws ClassNotFoundException, NoClassDefFoundError
    {
        return Class.forName(className);
    }

    @Override
    public boolean hasClassAnnotation(Class clazz, String annotationClassName) throws ClassNotFoundException, NoClassDefFoundError
    {
        final Class annotationClass = Class.forName(annotationClassName);
        return null != clazz.getAnnotation(annotationClass);
    }

    @Override
    public Set<Method> getMethodsWithAnnotation(Class clazz, String annotationClassName) throws ClassNotFoundException, NoClassDefFoundError
    {
        final Class annotationClass = Class.forName(annotationClassName);
        return Sets.filter(new HashSet<Method>(asList(clazz.getMethods())), new Predicate<Method>()
        {
            @Override
            public boolean apply(@Nullable Method method)
            {
                return null != method && null != method.getAnnotation(annotationClass);
            }
        });
    }
}
