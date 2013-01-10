package com.atlassian.plugin.remotable.kit.js.ringojs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.ringojs.wrappers.ScriptableList;

import java.lang.reflect.Method;
import java.util.List;

public class MyScriptableList extends ScriptableList
{
    private final List<Object> list;

    public MyScriptableList(Scriptable scope, List<Object> list)
    {
        super(scope, list);
        this.list = list;
    }

    @Override
    public boolean has(String name, Scriptable start)
    {
        return name.equals("toJSON") || super.has(name, start);
    }

    @Override
    public Object get(String name, Scriptable start)
    {
        return name.equals("toJSON") ? newFunction("toJSON") : super.get(name, start);
    }

    private FunctionObject newFunction(String name)
    {
        try
        {
            Method target = getClass().getMethod("toJSON",
                new Class[] {Context.class, Scriptable.class, Object[].class, Function.class});
            return new FunctionObject(name, target, this);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Object toJSON(Context context, Scriptable thisObj, Object[] args, Function fn)
    {
        return context.newArray(thisObj, (((MyScriptableList) thisObj).list).toArray());
    }
}
