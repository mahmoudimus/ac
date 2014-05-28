package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.json.schema.annotation.SchemaDefinition;

@SchemaDefinition("addonScope")
public class AddOnScopeBean
{
    private String key; // set by gson
    private Collection<String> restPathKeys; // set by gson
    private Collection<String> soapRpcPathKeys; // set by gson
    private Collection<String> jsonRpcPathKeys; // set by gson
    private Collection<String> xmlRpcPathKeys; // set by gson
    private Collection<String> pathKeys; // set by gson
    private Collection<String> methods; // set by gson

    public AddOnScopeBean()
    {
        this(null, null, null, null, null, null, null);
    }

    public AddOnScopeBean(String key, Collection<String> restPathKeys, Collection<String> soapRpcPathKeys,
                          Collection<String> jsonRpcPathKeys, Collection<String> xmlRpcPathKeys, Collection<String> pathKeys,
                          Collection<String> methods)
    {
        this.key = key;
        this.restPathKeys = restPathKeys;
        this.soapRpcPathKeys = soapRpcPathKeys;
        this.jsonRpcPathKeys = jsonRpcPathKeys;
        this.xmlRpcPathKeys = jsonRpcPathKeys;
        this.pathKeys = pathKeys;
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

    public Collection<String> getJsonRpcPathKeys()
    {
        return null == jsonRpcPathKeys ? Collections.<String>emptySet() : jsonRpcPathKeys;
    }

    public Collection<String> getXmlRpcPathKeys()
    {
        return null == xmlRpcPathKeys ? Collections.<String>emptySet() : xmlRpcPathKeys;
    }

    public Collection<String> getPathKeys()
    {
        return null == pathKeys ? Collections.<String>emptySet() : pathKeys;
    }

    public Collection<String> getMethods()
    {
        return methods;
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

    public static class RpcBean
    {
        private String key; // set by gson, must be unique within the JSON scopes file
        private Collection<String> rpcMethods; // set by gson

        public RpcBean()
        {
            this(null, null);
        }

        public RpcBean(String key, Collection<String> rpcMethods)
        {
            this.key = key;
            this.rpcMethods = rpcMethods;
        }

        public String getKey()
        {
            return key;
        }

        public Collection<String> getRpcMethods()
        {
            return rpcMethods;
        }
    }

    public static class RpcPathBean extends RpcBean
    {
        private Collection<String> paths; // set by gson

        public RpcPathBean()
        {
            this(null, null, null);
        }

        public RpcPathBean(String key, Collection<String> paths, Collection<String> rpcMethods)
        {
            super(key, rpcMethods);
            this.paths = paths;
        }

        public Collection<String> getPaths()
        {
            return paths;
        }
    }

    public static class SoapRpcPathBean extends RpcPathBean
    {
        public SoapRpcPathBean()
        {
            super();
        }

        public SoapRpcPathBean(String key, Collection<String> paths, Collection<String> rpcMethods)
        {
            super(key, paths, rpcMethods);
        }
    }

    public static class JsonRpcPathBean extends RpcPathBean
    {
        public JsonRpcPathBean()
        {
            super();
        }

        public JsonRpcPathBean(String key, Collection<String> paths, Collection<String> rpcMethods)
        {
            super(key, paths, rpcMethods);
        }
    }

    public static class XmlRpcBean extends RpcBean
    {
        private Collection<String> prefixes; // set by gson

        public XmlRpcBean()
        {
            this(null, null, null);
        }

        public XmlRpcBean(String key, Collection<String> prefixes, Collection<String> rpcMethods)
        {
            super(key, rpcMethods);
            this.prefixes = prefixes;
        }

        public Collection<String> getPrefixes()
        {
            return prefixes;
        }
    }

    public static class PathBean
    {
        private String key; // set by gson, must be unique within paths within the JSON scopes file
        private Collection<String> paths; // set by gson

        public PathBean()
        {
            this(null, null);
        }

        public PathBean(String key, Collection<String> paths)
        {
            this.key = key;
            this.paths = paths;
        }

        public String getKey()
        {
            return key;
        }

        public Collection<String> getPaths()
        {
            return paths;
        }
    }
}
