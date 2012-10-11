package com.atlassian.plugin.remotable.host.common.service.http;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.atlassian.util.concurrent.Deferred;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.xmlrpc.ServiceBean;
import com.atlassian.xmlrpc.ServiceBeanField;
import com.atlassian.xmlrpc.ServiceMethod;
import com.atlassian.xmlrpc.ServiceObject;
import com.atlassian.xmlrpc.XmlRpcClientProvider;
import com.google.common.util.concurrent.FutureCallback;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * {@link java.lang.reflect.InvocationHandler} for the XML-RPC service object proxy
 *
 * Copied from atlassian-xmlrpc but modified to support promises
 *
 * @author jdumay
 */
public final class PromiseAwareXmlRpcInvocationHandler implements InvocationHandler
{
    private XmlRpcClientProvider clientProvider;

    public PromiseAwareXmlRpcInvocationHandler(XmlRpcClientProvider clientProvider)
    {
        this.clientProvider = clientProvider;
    }

    /**
     * Extracts the XML-RPC method name from the {@link java.lang.reflect.Method}
     * @param method
     * @return methodName
     */
    protected String getMethodName(Method method)
    {
        String methodName = method.getName();
        ServiceMethod serviceMethod = method.getAnnotation(ServiceMethod.class);
        if (serviceMethod != null && serviceMethod.value() != null)
        {
            methodName = serviceMethod.value();
        }
        return methodName;
    }

    /**
     * Converts arguments from an array of objects to a {@link java.util.Vector} and converts any {@link java.util.Collection}'s to an Array
     *
     * @param objects
     * @return
     */
    protected Vector convertArguments(Object[] objects)
    {
        //When there are no arguments to the method
        if (objects == null)
        {
            return new Vector();
        }

        final ArrayList args = new ArrayList();
        for (Object o : objects)
        {
            if (o instanceof Collection)
            {
                args.add(((Collection) o).toArray());
            }
            else
            {
                args.add(o);
            }
        }
        return new Vector(args);
    }

    /**
     * Converts the return value of a method to a {@link com.atlassian.xmlrpc.ServiceBean} marked object (if metadata is available)
     * @param method
     * @param returnValue
     * @return returnValue converted value
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    protected Object convertReturnValue(final Method method, Object returnValue)
            throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        if (returnValue == null)
        {
            return null;
        }

        final Class beanType = getReturnBeanType(method);

        Collection collection = null;
        if (returnValue.getClass().isArray())
        {
            Object[] objArray = (Object[]) returnValue;
            collection = new ArrayList(Arrays.asList(objArray));

            if (beanType != null)
            {
                collection = mapBeanCollection(collection, beanType);
            }

            return collection;
        }
        else if (Collection.class.isAssignableFrom(returnValue.getClass()))
        {
            collection = (Collection)returnValue;
            if (beanType != null)
            {
                collection = mapBeanCollection(collection, beanType);
            }

            return collection;
        }
        else if (Map.class.isAssignableFrom(returnValue.getClass()) && beanType != null)
        {
            returnValue = mapBean((Map) returnValue, beanType);
        }
        else if (Promise.class.isAssignableFrom(returnValue.getClass()))
        {
            final Deferred<Object> deferred = Deferred.create();

            Promise<Object> actualPromise = (Promise<Object>) returnValue;
            actualPromise.on(new FutureCallback<Object>()
            {
                @Override
                public void onSuccess(Object result)
                {
                    try
                    {
                        deferred.resolve(convertReturnValue(method, result));
                    }
                    catch (Exception e)
                    {
                        deferred.reject(e);
                    }
                }

                @Override
                public void onFailure(Throwable t)
                {
                    deferred.reject(t);
                }
            });
            returnValue = deferred.promise();
        }
        return returnValue;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        final String serviceName = getServiceName(method);
        final String methodName = getMethodName(method);
        try
        {
            Object returnValue = clientProvider.execute(serviceName, methodName, convertArguments(args));
            returnValue = convertReturnValue(method, returnValue);

            //If the return type is null and method return typoe is void we shouldn't return any value
            if (returnValue != null && !method.getReturnType().equals(Void.class))
            {
                return returnValue;
            }
            return Void.TYPE;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not execute RPC method " + methodName, e);
        }
    }

    private String getServiceName(Method method)
    {
        ServiceObject serviceObject = method.getDeclaringClass().getAnnotation(ServiceObject.class);
        return serviceObject.value();
    }

    private Class getReturnBeanType(Method method)
    {
        //Check if the return type is the service bean
        if (method.getReturnType().isAnnotationPresent(ServiceBean.class))
        {
            return method.getReturnType();
        }

        //Check if the return bean type is a generic parameter of a different type
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType)
        {
            ParameterizedType typeParam = (ParameterizedType) type;
            if (typeParam.getActualTypeArguments().length == 1)
            {
                Type resultType = typeParam.getActualTypeArguments()[0];
                if (resultType instanceof Class)
                {
                    Class returnType = (Class) resultType;
                    if (returnType.isAnnotationPresent(ServiceBean.class))
                    {
                        return returnType;
                    }
                }
            }
        }

        return null;
    }

    private ArrayList mapBeanCollection(Collection<Map> result, Class beanType)
            throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        ArrayList beanCollection = new ArrayList();

        for (Map map : result)
        {
            final Object beanInstance = mapBean(map, beanType);
            beanCollection.add(beanInstance);
        }

        return beanCollection;
    }

    private Object mapBean(Map map, Class beanType)
            throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        Object beanInstance = beanType.newInstance();
        mapToBean(beanInstance, map);
        return beanInstance;
    }

    private void mapToBean(Object bean, Map map)
            throws IllegalAccessException, InvocationTargetException
    {
        BeanUtils.populate(bean, removeNullValues(mapArraysToLists(remapPropertyNames(bean, map))));
    }

    private Map mapArraysToLists(Map<String, Object> map)
    {
        Map<String, Object> result = new HashMap<String, Object>(map);
        for (String key : result.keySet())
        {
            Object value = result.get(key);
            if (value != null && value.getClass().isArray())
            {
                result.put(key, Arrays.asList((Object[]) value));
            }
            else
            {
                result.put(key, value);
            }
        }
        return result;
    }

    private Map removeNullValues(Map<String, Object> map)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : map.keySet())
        {
            final Object value = map.get(key);
            /*
             Nil values may be returned as empty strings from some XML-RPC servers
             Until we have some nice way of abstracting the type conversion out on
             a per binder impl basis this hack will have todo
            */
            final boolean isString = value instanceof String;
            final boolean isEmptyString = isString && value.toString().equals("");

            if (value != null && !isEmptyString)
            {
                result.put(key, value);
            }
        }
        return result;
    }

    private Map remapPropertyNames(Object bean, Map map)
    {
        Map result = new HashMap(map);
        for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(bean))
        {
            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod != null)
            {
                ServiceBeanField beanField = writeMethod.getAnnotation(ServiceBeanField.class);
                if (beanField != null && beanField.value() != null && !beanField.value().equals(""))
                {
                    Object data = result.get(beanField.value());
                    result.remove(beanField.value());
                    result.put(descriptor.getName(), data);
                }
                else
                {
                    result.put(descriptor.getName(), map.get(descriptor.getName()));
                }
            }
        }
        return result;
    }
}
