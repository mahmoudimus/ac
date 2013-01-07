package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.google.common.base.Function;
import org.mozilla.javascript.NativeJavaMethod;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import java.lang.reflect.Method;

public class MyNativeJavaObject extends NativeJavaObject
{
    private final Class<?> staticType;

    public MyNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType)
    {
        super(scope, javaObject, staticType);
        this.staticType = staticType != null ? staticType : javaObject.getClass();
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
                        return new MyNativeJavaMethod((NativeJavaMethod)result, method);
                    }
                }
            }

            return result;
        }

        return result;
    }
}
