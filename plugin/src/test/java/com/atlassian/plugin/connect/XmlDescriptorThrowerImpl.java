package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.modules.schema.ConnectDescriptorValidator;
import com.atlassian.plugin.connect.spi.XmlDescriptor;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@XmlDescriptor
public class XmlDescriptorThrowerImpl implements XmlDescriptorThrower
{
    private static final Logger log = LoggerFactory.getLogger(XmlDescriptorThrowerImpl.class);

    public XmlDescriptorThrowerImpl(ConnectDescriptorValidator someClassInstanceFromThePluginClassLoader)
    {
        log.warn("Injecting dynamic proxies that prevent XML descriptor code from running.");
        injectProxies(someClassInstanceFromThePluginClassLoader);
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
    private static void injectProxies(Object someClassInstanceFromThePluginClassLoader)
    {
        ThrowingProxy allMethodsProxy = new ThrowingProxy(new AllMethodsMatcher());

        // TODO: is there an Atlassian spring scanner class that does this?
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().includePackage("com.atlassian.plugin.connect"))
                .setScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner())
                .addClassLoader(someClassInstanceFromThePluginClassLoader.getClass().getClassLoader())
                .build());
        Map<Class, ThrowingProxy> proxiedClasses = new HashMap<Class, ThrowingProxy>();

        // TODO: always returns an empty set
        for (Class clazz : reflections.getTypesAnnotatedWith(XmlDescriptor.class, true))
        {
            injectProxy(proxiedClasses, clazz, allMethodsProxy);
        }

        // TODO: always returns an empty set
        for (Method method : reflections.getMethodsAnnotatedWith(XmlDescriptor.class))
        {
            // don't proxy a method if the whole class has been proxied
            // TODO: I *think* that getDeclaringClass() would be wrong... should check this
            final Class<? extends Method> clazz = method.getClass();

            if (proxiedClasses.containsKey(clazz))
            {
                proxiedClasses.get(clazz).addMethod(method);
            }
            else
            {
                ThrowingProxy proxy = new ThrowingProxy(new SomeMethodsMatcher(method));
                injectProxy(proxiedClasses, clazz, proxy);
            }
        }
    }

    private static void injectProxy(Map<Class, ThrowingProxy> proxiedClasses, Class<? extends Method> clazz, ThrowingProxy proxy)
    {
        Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, proxy);
        proxiedClasses.put(clazz, proxy);
    }
}
