package com.atlassian.plugin.connect.xmldescriptor;

import java.lang.reflect.Method;
import java.util.Set;

public interface AnnotationService
{
    public Class loadClass(String className) throws ClassNotFoundException, NoClassDefFoundError;

    public boolean hasClassAnnotation(Class clazz, String annotationClassName) throws ClassNotFoundException, NoClassDefFoundError;

    public Set<Method> getMethodsWithAnnotation(Class clazz, String annotationClassName) throws ClassNotFoundException, NoClassDefFoundError;
}
