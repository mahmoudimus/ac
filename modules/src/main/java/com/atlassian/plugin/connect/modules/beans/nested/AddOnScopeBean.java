package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;

public class AddOnScopeBean
{
    private String key; // set by gson
    private Collection<String> restPaths; // set by gson
    private Collection<String> methods; // set by gson

    public AddOnScopeBean()
    {
        this(null, null, null);
    }

    public AddOnScopeBean(String key, Collection<String> restPaths, Collection<String> methods)
    {
        this.key = key;
        this.restPaths = restPaths;
        this.methods = methods;
    }

    public String getKey()
    {
        return key;
    }

    public Collection<String> getRestPaths()
    {
        return restPaths;
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
}
