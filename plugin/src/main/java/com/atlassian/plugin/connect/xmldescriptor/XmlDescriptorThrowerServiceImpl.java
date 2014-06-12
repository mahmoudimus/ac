package com.atlassian.plugin.connect.xmldescriptor;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ExportAsDevService
@Named("xmlDescriptorThrowerService")
public class XmlDescriptorThrowerServiceImpl implements XmlDescriptorThrowerService
{
    private static final Logger log = LoggerFactory.getLogger(XmlDescriptorThrowerServiceImpl.class);
    private static final String ANNOTATION_NAME = XmlDescriptor.class.getName();

    private final AnnotationService annotationService;

    @Inject
    public XmlDescriptorThrowerServiceImpl(AnnotationService annotationService)
    {
        this.annotationService = annotationService;
    }

    @Override
    public Set<String> runAndGetProxyFailures()
    {
        return injectProxies();
    }

    private static interface MethodsMatcher
    {
        boolean matches(Method method);
        void addMethod(Method method);
    }

    private static class AllMethodsMatcher implements MethodsMatcher
    {
        @Override
        public boolean matches(Method method)
        {
            return true;
        }

        @Override
        public void addMethod(Method method)
        {
            // do nothing
        }
    }

    private static class SomeMethodsMatcher implements MethodsMatcher
    {
        private Set<Method> methods;

        public SomeMethodsMatcher(Method method)
        {
            this.methods =  new HashSet<Method>();
            this.methods.add(method);
        }

        @Override
        public boolean matches(Method method)
        {
            return methods.contains(method);
        }

        @Override
        public void addMethod(Method method)
        {
            this.methods.add(method);
        }
    }

    // throw when a @XmlDescriptor method is invoked
    private static class ThrowingProxy implements InvocationHandler
    {
        private MethodsMatcher methodsMatcher;

        public ThrowingProxy(MethodsMatcher methodsMatcher)
        {
            this.methodsMatcher = methodsMatcher;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (methodsMatcher.matches(method))
            {
                final String message = String.format("%s.%s() should not be called (it is marked as @%s)",
                        proxy.getClass().getSimpleName(), method.getName(), XmlDescriptor.class.getSimpleName());
                log.warn(message);
                throw new Error(message); // not RuntimeException because we don't want it caught
            }
            else
            {
                return method.invoke(proxy, args);
            }
        }

        void addMethod(Method method)
        {
            methodsMatcher.addMethod(method);
        }
    }

    // find types and methods annotated with @XmlDescriptor and cause them to throw
    private Set<String> injectProxies()
    {
        log.warn("Injecting dynamic proxies that prevent XML descriptor code from running.");
        Map<Class, ThrowingProxy> proxiedClasses = new HashMap<Class, ThrowingProxy>();
        Set<String> proxyFailures = new HashSet<String>(); // we can't proxy these classes (e.g. they don't implement interfaces)

        injectProxies(proxiedClasses, proxyFailures);

        log.debug("Injected dynamic proxies around this many classes: {}", proxiedClasses.size());
        log.debug(proxiedClasses.keySet().toString());
        log.debug("Failed to proxy this many classes: {}", proxyFailures.size());
        return proxyFailures;
    }

    private void injectProxies(final Map<Class, ThrowingProxy> proxiedClasses, final Set<String> proxyFailures)
    {
        final ThrowingProxy allMethodsProxy = new ThrowingProxy(new AllMethodsMatcher());

        // cd from "plugin/target/container/tomcat7x/cargo-jira-home/" to "plugin/target/classes/com/atlassian/plugin/connect/"
        Path classFilesBasePath = FileSystems.getDefault().getPath("../../../classes/com/atlassian/plugin/connect");
        try
        {
            Files.walkFileTree(classFilesBasePath, new FileVisitor<Path>()
            {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    if (file.getFileName().toString().endsWith(".class"))
                    {
                        final String className = file.toString().replaceFirst(".*classes/(.+)\\.class", "$1").replace('/', '.');

                        try
                        {
                            final Class clazz = annotationService.loadClass(className);

                            if (annotationService.hasClassAnnotation(clazz, ANNOTATION_NAME))
                            {
                                injectProxy(clazz, allMethodsProxy, proxiedClasses, proxyFailures);
                            }
                            else
                            {
                                final Set<Method> methods = annotationService.getMethodsWithAnnotation(clazz, ANNOTATION_NAME);

                                if (!methods.isEmpty())
                                {
                                    injectProxies(clazz, methods, proxiedClasses, proxyFailures);
                                }
                            }
                        }
                        catch (ClassNotFoundException e)
                        {
                            log.error("Class not found: '{}'", className);
                            proxyFailures.add(className);
                        }
                        catch (NoClassDefFoundError e)
                        {
                            log.error("No class definition: '{}'", className);
                            proxyFailures.add(className);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
                {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void injectProxy(Class clazz, ThrowingProxy proxy, Map<Class, ThrowingProxy> proxiedClasses, Set<String> proxyFailures)
    {
        if (clazz.isInterface())
        {
            Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, proxy);
            proxiedClasses.put(clazz, proxy);
        }
        else
        {
            Class[] interfaces = clazz.getInterfaces();

            if (interfaces.length == 0)
            {
                proxyFailures.add(clazz.getName());
            }
            else
            {
                for (Class interfaze : interfaces)
                {
                    injectProxy(interfaze, proxy, proxiedClasses, proxyFailures);
                }

                proxiedClasses.put(clazz, proxy);
            }
        }
    }

    private static void injectProxies(Class clazz, Set<Method> methods, Map<Class, ThrowingProxy> proxiedClasses, Set<String> proxyFailures)
    {
        for (Method method : methods)
        {
            ThrowingProxy proxy = proxiedClasses.get(clazz);

            if (null == proxy)
            {
                injectProxy(clazz, new ThrowingProxy(new SomeMethodsMatcher(method)), proxiedClasses, proxyFailures);
            }
            else
            {
                proxy.addMethod(method);
            }
        }
    }
}
