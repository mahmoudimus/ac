package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;
import java.util.Collections;

public class AddOnScopeBean
{
    private String key; // set by gson
    private Collection<String> restPathKeys; // set by gson
    private Collection<String> soapRpcPathKeys; // set by gson
    private Collection<String> jsonRpcPathKeys; // set by gson
    private Collection<String> methods; // set by gson

    public AddOnScopeBean()
    {
        this(null, null, null, null, null);
    }

    public AddOnScopeBean(String key, Collection<String> restPathKeys, Collection<String> soapRpcPathKeys, Collection<String> jsonRpcPathKeys, Collection<String> methods)
    {
        this.key = key;
        this.restPathKeys = restPathKeys;
        this.soapRpcPathKeys = soapRpcPathKeys;
        this.jsonRpcPathKeys = jsonRpcPathKeys;
        this.methods = methods;
    }

    public String getKey()
    {
        return key;
    }

    public Collection<String> getRestPathKeys()
    {
        return null == restPathKeys ? Collections.<String>emptySet() : restPathKeys;
    }

    public Collection<String> getSoapRpcPathKeys()
    {
        return null == soapRpcPathKeys ? Collections.<String>emptySet() : soapRpcPathKeys;
    }

    public Collection<String> getMethods()
    {
        return methods;
    }

    public Collection<String> getJsonRpcPathKeys()
    {
        return null == jsonRpcPathKeys ? Collections.<String>emptySet() : jsonRpcPathKeys;
    }

    public static class RestPathBean
    {
        private String key; // set by gson, must be unique within the JSON scopes file
        private String name; // set by gson
        private Collection<String> basePaths; // set by gson
        private Collection<String> versions; // set by gson

        public RestPathBean()
        {
            this.key = null;
            this.name = null;
            this.basePaths = null;
            this.versions = null;
        }

        public RestPathBean(String key, String name, Collection<String> basePaths, Collection<String> versions)
        {
            this.key = key;
            this.name = name;
            this.basePaths = basePaths;
            this.versions = versions;
        }

        public String getKey()
        {
            return key;
        }

        public String getName()
        {
            return name;
        }

        public Collection<String> getBasePaths()
        {
            return basePaths;
        }

        public Collection<String> getVersions()
        {
            return versions;
        }
    }

    public static class RpcPathBean
    {
        private String key; // set by gson, must be unique within the JSON scopes file
        private String path; // set by gson
        private Collection<String> rpcMethods; // set by gson

        public RpcPathBean()
        {
            this(null, null, null);
        }

        public RpcPathBean(String key, String path, Collection<String> rpcMethods)
        {
            this.key = key;
            this.path = path;
            this.rpcMethods = rpcMethods;
        }

        public String getKey()
        {
            return key;
        }

        public String getPath()
        {
            return path;
        }

        public Collection<String> getRpcMethods()
        {
            return rpcMethods;
        }
    }

    public static class SoapRpcPathBean extends RpcPathBean
    {
        public SoapRpcPathBean()
        {
            super();
        }

        public SoapRpcPathBean(String key, String path, Collection<String> rpcMethods)
        {
            super(key, path, rpcMethods);
        }
    }

    public static class JsonRpcPathBean extends RpcPathBean
    {
        public JsonRpcPathBean()
        {
            super();
        }

        public JsonRpcPathBean(String key, String path, Collection<String> rpcMethods)
        {
            super(key, path, rpcMethods);
        }
    }
}
