package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.google.common.base.Function;
import org.mozilla.javascript.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MyNativeJavaMethod implements Scriptable, org.mozilla.javascript.Function
{
    private final BaseFunction delegate;
    private final Method method;

    public MyNativeJavaMethod(NativeJavaMethod result, Method method)
    {
        this.delegate = result;
        this.method = method;
    }

    @Override
    public Object call(Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args)
    {
        Object[] params = new Object[args.length];
        for (int x=0; x<params.length; x++)
        {
            if (args[x] instanceof NativeFunction && Function.class.isAssignableFrom(method.getParameterTypes()[x]))
            {
                final NativeFunction fn = (NativeFunction) args[x];
                final ParameterizedType type = (ParameterizedType) method.getGenericParameterTypes()[x];
                final Type[] typeArgs = type.getActualTypeArguments();
                final Class inType = convertToClass(typeArgs[0]);
                final Class outType = convertToClass(typeArgs[1]);
                params[x] = new Function() {
                    @Override
                    public Object apply(Object o)
                    {
                        Context context = Context.enter();
                        try
                        {
                            Object[] objects = {context.getWrapFactory().wrap(context, scope, o, inType)};
                            Object value = fn.call(context, scope, scope, objects);
                            return Context.jsToJava(value, outType);
                        }
                        finally
                        {
                            Context.exit();
                        }
                    }
                };
            }
            else
            {
                params[x] = args[x];
            }
        }
        return delegate.call(cx, scope, thisObj, params);
    }

    private Class convertToClass(Type typeArg)
    {
        return typeArg instanceof Class ? (Class) typeArg : Object.class;
    }

    public Scriptable construct(Context cx, Scriptable scope, Object[] args)
    {
        return delegate.construct(cx, scope, args);
    }

    public Scriptable createObject(Context cx, Scriptable scope)
    {
        return delegate.createObject(cx, scope);
    }

    public int getArity()
    {
        return delegate.getArity();
    }

    public int getLength()
    {
        return delegate.getLength();
    }

    public String getFunctionName()
    {
        return delegate.getFunctionName();
    }

    @Override
    public boolean has(String name, Scriptable start)
    {
        return delegate.has(name, start);
    }

    @Override
    public Object get(String name, Scriptable start)
    {
        return delegate.get(name, start);
    }

    @Override
    public void put(String name, Scriptable start, Object value)
    {
        delegate.put(name, start, value);
    }

    @Override
    public void delete(String name)
    {
        delegate.delete(name);
    }

    public int getAttributes(String name)
    {
        return delegate.getAttributes(name);
    }

    public void setAttributes(String name, int attributes)
    {
        delegate.setAttributes(name, attributes);
    }

    public void defineOwnProperty(Context cx, Object key, ScriptableObject desc)
    {
        delegate.defineOwnProperty(cx, key, desc);
    }

    @Override
    public boolean has(int index, Scriptable start)
    {
        return delegate.has(index, start);
    }

    @Override
    public Object get(int index, Scriptable start)
    {
        return delegate.get(index, start);
    }

    @Override
    public void put(int index, Scriptable start, Object value)
    {
        delegate.put(index, start, value);
    }

    @Override
    public void delete(int index)
    {
        delegate.delete(index);
    }

    public void putConst(String name, Scriptable start, Object value)
    {
        delegate.putConst(name, start, value);
    }

    public void defineConst(String name, Scriptable start)
    {
        delegate.defineConst(name, start);
    }

    public boolean isConst(String name)
    {
        return delegate.isConst(name);
    }

    public void setAttributes(int index, Scriptable start, int attributes)
    {
        delegate.setAttributes(index, start, attributes);
    }

    public int getAttributes(int index)
    {
        return delegate.getAttributes(index);
    }

    public void setAttributes(int index, int attributes)
    {
        delegate.setAttributes(index, attributes);
    }

    public void setGetterOrSetter(String name, int index, Callable getterOrSetter, boolean isSetter)
    {
        delegate.setGetterOrSetter(name, index, getterOrSetter, isSetter);
    }

    public Object getGetterOrSetter(String name, int index, boolean isSetter)
    {
        return delegate.getGetterOrSetter(name, index, isSetter);
    }

    @Override
    public Scriptable getPrototype()
    {
        return delegate.getPrototype();
    }

    @Override
    public void setPrototype(Scriptable m)
    {
        delegate.setPrototype(m);
    }

    @Override
    public Scriptable getParentScope()
    {
        return delegate.getParentScope();
    }

    @Override
    public void setParentScope(Scriptable m)
    {
        delegate.setParentScope(m);
    }

    @Override
    public Object[] getIds()
    {
        return delegate.getIds();
    }

    public Object[] getAllIds()
    {
        return delegate.getAllIds();
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint)
    {
        return delegate.getDefaultValue(typeHint);
    }

    public void defineProperty(String propertyName, Object value, int attributes)
    {
        delegate.defineProperty(propertyName, value, attributes);
    }

    public static void defineProperty(Scriptable destination, String propertyName, Object value, int attributes)
    {
        ScriptableObject.defineProperty(destination, propertyName, value, attributes);
    }

    public static void defineConstProperty(Scriptable destination, String propertyName)
    {
        ScriptableObject.defineConstProperty(destination, propertyName);
    }

    public void defineProperty(String propertyName, Class<?> clazz, int attributes)
    {
        delegate.defineProperty(propertyName, clazz, attributes);
    }

    public void defineProperty(String propertyName, Object delegateTo, Method getter, Method setter, int attributes)
    {
        delegate.defineProperty(propertyName, delegateTo, getter, setter, attributes);
    }

    public void defineOwnProperties(Context cx, ScriptableObject props)
    {
        delegate.defineOwnProperties(cx, props);
    }

    public void defineFunctionProperties(String[] names, Class<?> clazz, int attributes)
    {
        delegate.defineFunctionProperties(names, clazz, attributes);
    }

    public static Scriptable getObjectPrototype(Scriptable scope)
    {
        return ScriptableObject.getObjectPrototype(scope);
    }

    public static Scriptable getFunctionPrototype(Scriptable scope)
    {
        return ScriptableObject.getFunctionPrototype(scope);
    }

    public static Scriptable getArrayPrototype(Scriptable scope)
    {
        return ScriptableObject.getArrayPrototype(scope);
    }

    public static Scriptable getClassPrototype(Scriptable scope, String className)
    {
        return ScriptableObject.getClassPrototype(scope, className);
    }

    public static Scriptable getTopLevelScope(Scriptable obj)
    {
        return ScriptableObject.getTopLevelScope(obj);
    }

    public boolean isExtensible()
    {
        return delegate.isExtensible();
    }

    public void preventExtensions()
    {
        delegate.preventExtensions();
    }

    public void sealObject()
    {
        delegate.sealObject();
    }

    public static Object getProperty(Scriptable obj, String name)
    {
        return ScriptableObject.getProperty(obj, name);
    }

    public static <T> T getTypedProperty(Scriptable s, int index, Class<T> type)
    {
        return ScriptableObject.getTypedProperty(s, index, type);
    }

    public static Object getProperty(Scriptable obj, int index)
    {
        return ScriptableObject.getProperty(obj, index);
    }

    public static <T> T getTypedProperty(Scriptable s, String name, Class<T> type)
    {
        return ScriptableObject.getTypedProperty(s, name, type);
    }

    public static boolean hasProperty(Scriptable obj, String name)
    {
        return ScriptableObject.hasProperty(obj, name);
    }

    public static void redefineProperty(Scriptable obj, String name, boolean isConst)
    {
        ScriptableObject.redefineProperty(obj, name, isConst);
    }

    public static boolean hasProperty(Scriptable obj, int index)
    {
        return ScriptableObject.hasProperty(obj, index);
    }

    public static void putProperty(Scriptable obj, String name, Object value)
    {
        ScriptableObject.putProperty(obj, name, value);
    }

    public static void putConstProperty(Scriptable obj, String name, Object value)
    {
        ScriptableObject.putConstProperty(obj, name, value);
    }

    public static void putProperty(Scriptable obj, int index, Object value)
    {
        ScriptableObject.putProperty(obj, index, value);
    }

    public static boolean deleteProperty(Scriptable obj, String name)
    {
        return ScriptableObject.deleteProperty(obj, name);
    }

    public static boolean deleteProperty(Scriptable obj, int index)
    {
        return ScriptableObject.deleteProperty(obj, index);
    }

    public static Object[] getPropertyIds(Scriptable obj)
    {
        return ScriptableObject.getPropertyIds(obj);
    }

    public static Object callMethod(Scriptable obj, String methodName, Object[] args)
    {
        return ScriptableObject.callMethod(obj, methodName, args);
    }

    public static Object callMethod(Context cx, Scriptable obj, String methodName, Object[] args)
    {
        return ScriptableObject.callMethod(cx, obj, methodName, args);
    }

    public static Object getTopScopeValue(Scriptable scope, Object key)
    {
        return ScriptableObject.getTopScopeValue(scope, key);
    }

    public int size()
    {
        return delegate.size();
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public Object get(Object key)
    {
        return delegate.get(key);
    }


    public static <T extends Scriptable> String defineClass(Scriptable scope, Class<T> clazz, boolean sealed, boolean mapInheritance) throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        return ScriptableObject.defineClass(scope, clazz, sealed, mapInheritance);
    }

    public static <T extends Scriptable> void defineClass(Scriptable scope, Class<T> clazz, boolean sealed) throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        ScriptableObject.defineClass(scope, clazz, sealed);
    }

    public static <T extends Scriptable> void defineClass(Scriptable scope, Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        ScriptableObject.defineClass(scope, clazz);
    }

    public boolean avoidObjectDetection()
    {
        return delegate.avoidObjectDetection();
    }

    public static Object getDefaultValue(Scriptable object, Class<?> typeHint)
    {
        return ScriptableObject.getDefaultValue(object, typeHint);
    }

    @Override
    public String getClassName()
    {
        return delegate.getClassName();
    }

    public String getTypeOf()
    {
        return delegate.getTypeOf();
    }

    @Override
    public boolean hasInstance(Scriptable instance)
    {
        return delegate.hasInstance(instance);
    }

    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
    {
        return delegate.execIdCall(f, cx, scope, thisObj, args);
    }

    public void setImmunePrototypeProperty(Object value)
    {
        delegate.setImmunePrototypeProperty(value);
    }
}
