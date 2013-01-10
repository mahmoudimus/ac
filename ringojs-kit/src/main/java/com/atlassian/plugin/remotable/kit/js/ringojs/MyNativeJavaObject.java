package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.google.common.base.Function;
import org.mozilla.javascript.NativeJavaMethod;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class MyNativeJavaObject extends NativeJavaObject
{
    private final Class<?> staticType;
    private final ScriptExecutor executor;

    public MyNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType, ScriptExecutor executor)
    {
        super(scope, javaObject, staticType);
        this.executor = executor;
        this.staticType = staticType != null && staticType != Object.class ? staticType : javaObject.getClass();
    }

    @Override
    public Object get(String name, Scriptable start)
    {
        Object result = super.get(name, start);
        if (result instanceof NativeJavaMethod)
        {
            Method method = null;
            for (Method m : staticType.getMethods())
            {
                if (m.getName().equals(name))
                {
                    method = m;
                    break;
                }
            }
            if (method != null)
            {
                for (Class arg : method.getParameterTypes())
                {
                    if (Function.class.isAssignableFrom(arg))
                    {
                        return new MyNativeJavaMethod((NativeJavaMethod)result, method, executor);
                    }
                }
            }

            return result;
        }

        return result;
    }
}
