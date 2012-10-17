package com.atlassian.plugin.remotable.host.common.service.confluence;

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

import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromiseMapFunction;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.http.HostXmlRpcClient;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.util.RemoteName;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.plugin.util.ChainingClassLoader;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Collections2.*;
import static com.google.common.collect.Maps.*;
import static java.util.Collections.*;


/**
 * {@link java.lang.reflect.InvocationHandler} for the XML-RPC service object proxy
 *
 * Copied from atlassian-xmlrpc but modified to support promises
 *
 * @author jdumay
 */
public final class ClientInvocationHandler implements InvocationHandler
{
    private static final Logger log = LoggerFactory.getLogger(ClientInvocationHandler.class);
    private final String serviceName;
    private HostXmlRpcClient clientProvider;
    private final Set<String> permissions;
    private final String pluginKey;
    private final HostHttpClient httpClient;
    private final RequestContext requestContext;

    public ClientInvocationHandler(String serviceName, HostXmlRpcClient clientProvider,
                                   Set<String> permissions, String pluginKey, HostHttpClient httpClient,
                                   RequestContext requestContext)
    {
        this.serviceName = serviceName;
        this.clientProvider = clientProvider;
        this.permissions = permissions;
        this.pluginKey = pluginKey;
        this.httpClient = httpClient;
        this.requestContext = requestContext;
    }

    /**
     * Converts arguments from an array of objects to a {@link java.util.Vector} and converts any {@link java.util.Collection}'s to an Array
     *
     * @param args
     * @param expectedTypes
     * @return
     */
    protected Object[] convertArguments(Object[] args, Type[] expectedTypes)
    {
        if (args == null)
        {
            args = new Object[0];
        }
        Object[] argsWithToken = new Object[args.length + 1];
        argsWithToken[0] = "";
        for (int x = 1; x < argsWithToken.length; x++)
        {
            Object arg = args[x - 1];
            argsWithToken[x] = toRemote(arg, expectedTypes[x - 1]);
        }
        return argsWithToken;
    }

    private Object toRemote(Object arg, Type expectedType)
    {
        final Class immediateType = expectedType instanceof Class ? (Class) expectedType :
                expectedType instanceof ParameterizedType ? (Class) ((ParameterizedType) expectedType).getRawType() :
                        expectedType instanceof GenericArrayType ? Array.class : null;
        if (immediateType == null)
        {
            throw new IllegalArgumentException("Unexpected type");
        }

        final Type genericType = getGenericType(expectedType);
        if (arg == null)
        {
            return null;
        }
        else if (arg instanceof Integer ||
                arg instanceof Date ||
                arg instanceof String ||
                arg instanceof Boolean ||
                arg.getClass().isArray())
        {
            return arg;
        }
        else if (arg instanceof Iterable)
        {
            return transform(Lists.newArrayList((Iterable) arg), new Function<Object, Object>()
            {
                @Override
                public Object apply(@Nullable Object input)
                {
                    return toRemote(input, genericType);
                }
            });
        }
        else if (immediateType.isInterface())
        {
            return toRemoteMap(arg, immediateType);
        }
        else if (immediateType.isEnum())
        {
            try
            {
                return getEnumRemoteName((Enum) arg);
            }
            catch (NoSuchFieldException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        else
        {
            return String.valueOf(arg);
        }
    }

    static Object getEnumRemoteName(Enum arg) throws NoSuchFieldException
    {
        RemoteName remoteName = arg.getClass().getField(arg.name()).getAnnotation(RemoteName
                .class);
        return remoteName != null ? remoteName.value() : arg.name();
    }

    private Map toRemoteMap(Object arg, Class expectedType)
    {
        Map result = newHashMap();
        for (Map.Entry<String, Method> prop : getReadableProperties(expectedType).entrySet())
        {
            String name = prop.getKey();
            Method method = prop.getValue();
            try
            {

                Object value = toRemote(method.invoke(arg), method.getReturnType());
                if (value != null)
                {
                    result.put(name, value);
                }
            }
            catch (InvocationTargetException e)
            {
                throw new IllegalArgumentException(e.getCause());
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        return result;
    }

    private Object fromRemote(Object returnValue, Type expectedType)
    {
        final Class immediateType = expectedType instanceof Class ? (Class) expectedType : (Class) ((ParameterizedType) expectedType).getRawType();
        final Type genericType = getGenericType(expectedType);

        if (returnValue == null)
        {
            if (Iterable.class.isAssignableFrom(immediateType))
            {
                return emptySet();
            }
            else if (Map.class.isAssignableFrom(immediateType))
            {
                return emptyMap();
            }
            else
            {
                return null;
            }
        }
        else if (Promise.class.isAssignableFrom(immediateType))
        {
            // handle automatic translation of a returned url to an inputstream
            if (genericType instanceof Class && InputStream.class.isAssignableFrom((Class) genericType))
            {
                return ((Promise<String>) returnValue).flatMap(new Function<String, Promise<InputStream>>()
                {
                    @Override
                    public Promise<InputStream> apply(String result)
                    {
                        return httpClient.newRequest(convertAbsoluteUrlToRelative(result)).get().map(
                                ResponsePromiseMapFunction.<InputStream>builder()
                                        .ok(new Function<Response, InputStream>()
                                        {
                                            @Override
                                            public InputStream apply(Response response)
                                            {
                                                return response.getEntityStream();
                                            }
                                        })
                                        .others(ResponsePromiseMapFunction.<InputStream>newUnexpectedResponseFunction())
                                        .build());
                    }
                });
            }
            else
            {
                return ((Promise<Object>) returnValue).map(new Function<Object, Object>()
                {
                    @Override
                    public Object apply(@Nullable Object result)
                    {
                        return fromRemote(result, genericType);
                    }
                });
            }
        }
        else if (Collection.class.isAssignableFrom(returnValue.getClass()))
        {
            returnValue = transform((Collection) returnValue, new Function()
            {
                @Override
                public Object apply(@Nullable Object input)
                {
                    return fromRemote(input, genericType);
                }
            });
        }
        else if (Map.class.isAssignableFrom(returnValue.getClass()))
        {
            returnValue = mapBean((Map) returnValue, immediateType);
        }
        else if (Long.TYPE == immediateType)
        {
            returnValue = Long.parseLong(String.valueOf(returnValue));
        }
        else if (URI.class == immediateType)
        {
            returnValue = URI.create(String.valueOf(returnValue));
        }
        else if (immediateType.isEnum())
        {
            for (Field field : immediateType.getFields())
            {
                RemoteName remoteName = field.getAnnotation(RemoteName.class);
                if (remoteName != null && remoteName.value().equals(returnValue))
                {
                    try
                    {
                        returnValue = field.get(null);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new IllegalArgumentException(e);
                    }
                    break;
                }
            }
            if (!returnValue.getClass().isEnum())
            {
                log.warn("Enum value '{}' not found for type '{}'", returnValue, immediateType);
                returnValue = null;
            }
        }
        return returnValue;
    }

    private String convertAbsoluteUrlToRelative(String absoluteUrl)
    {
        String hostBaseUrl = requestContext.getHostBaseUrl();
        if (absoluteUrl.startsWith(hostBaseUrl))
        {
            return absoluteUrl.substring(hostBaseUrl.length());
        }
        else
        {
            throw new IllegalArgumentException("Absolute URL '" + absoluteUrl + "' doesn't match " +
                    "current host base url '" + hostBaseUrl + "'");
        }
    }

    private Type getGenericType(Type type)
    {
        if (type instanceof ParameterizedType)
        {
            ParameterizedType typeParam = (ParameterizedType) type;
            if (typeParam.getActualTypeArguments().length == 1)
            {
                return typeParam.getActualTypeArguments()[0];
            }
        }
        else if (type instanceof GenericArrayType)
        {
            return ((GenericArrayType) type).getGenericComponentType();
        }
        return null;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        validatePermissions(method);
        final String methodName = method.getName();
        try
        {
            Object[] arguments = convertArguments(args, method.getGenericParameterTypes());
            //Class returnType = getReturnBeanType(method);

            Object returnValue = clientProvider.invoke(serviceName + "." + methodName, Object.class, arguments);
            returnValue = fromRemote(returnValue, method.getGenericReturnType());

            //If the return type is null and method return type is void we shouldn't return any value
            if (returnValue != null && !method.getReturnType().equals(Void.class))
            {
                return returnValue;
            }
            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not execute RPC method " + methodName, e);
        }
    }

    private void validatePermissions(Method method)
    {
        RequirePermission permission = method.getAnnotation(RequirePermission.class);
        if (permission != null && !permissions.contains(permission.value()))
        {
            throw new PermissionDeniedException(pluginKey, "Not able to call method '" + method.getName() + "' due to not having "
                    + "asked for permission '" + permission.value() + "'");
        }
    }

    private Object mapBean(Map map, Class beanType)
    {
        return Proxy.newProxyInstance(new ChainingClassLoader(getClass().getClassLoader(), beanType.getClassLoader()), new Class[]{beanType},
                new BeanInvocationHandler(
                        removeNullValues(remapPropertyNames(beanType, map))));
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

    private Map remapPropertyNames(Class bean, Map map)
    {
        Map result = new HashMap(map);
        for (Map.Entry<String, Method> prop : getReadableProperties(bean).entrySet())
        {
            String name = prop.getKey();
            Method method = prop.getValue();
            String originalName = convertMethodName(method);
            if (!name.equals(originalName))
            {
                Object data = result.get(name);
                result.remove(name);
                result.put(originalName, data);
            }
            else
            {
                result.put(name, map.get(name));
            }
        }
        return result;
    }

    private Map<String, Method> getReadableProperties(Class beanClass)
    {
        Map<String, Method> props = newHashMap();
        for (PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(beanClass))
        {
            Method readMethod = descriptor.getReadMethod();
            if (readMethod != null)
            {
                RemoteName beanField = readMethod.getAnnotation(RemoteName.class);
                if (beanField != null && beanField.value() != null && !beanField.value().equals(""))
                {
                    props.put(beanField.value(), readMethod);
                }
                else
                {
                    props.put(descriptor.getName(), readMethod);
                }
            }
        }
        return props;
    }

    private String convertMethodName(Method method)
    {
        String rawName = method.getName();
        String capsName = null;
        if (rawName.startsWith("get"))
        {
            capsName = rawName.substring("get".length());
        }
        else if (rawName.startsWith("is"))
        {
            capsName = rawName.substring("is".length());
        }
        else
        {
            throw new IllegalArgumentException("Invalid method access: " + method.getName());
        }

        return Character.toLowerCase(capsName.charAt(0)) + capsName.substring(1);
    }

    private class BeanInvocationHandler implements InvocationHandler
    {
        private final Map<String, Object> data;

        private BeanInvocationHandler(Map<String, Object> data)
        {
            this.data = data;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            String propertyName = convertMethodName(method);

            return fromRemote(data.get(propertyName), method.getGenericReturnType());
        }
    }
}
