package com.atlassian.plugin.connect.plugin.scopes.beans;

import java.util.Collection;

public class AddOnScopeBean
{
    private String key; // set by gson
    private Collection<RestPathBean> restPaths; // set by gson

    public String getKey()
    {
        return key;
    }

    public Collection<RestPathBean> getRestPaths()
    {
        return restPaths;
    }

    public static class RestPathBean
    {
        private String name; // set by gson
        private Collection<String> basePaths; // set by gson
        private Collection<String> versions; // set by gson
        private Collection<String> methods; // set by gson

        public RestPathBean()
        {
            this.name = null;
            this.basePaths = null;
            this.versions = null;
            this.methods = null;
        }

        public RestPathBean(String name, Collection<String> basePaths, Collection<String> versions, Collection<String> methods)
        {
            this.name = name;
            this.basePaths = basePaths;
            this.versions = versions;
            this.methods = methods;
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

        public Collection<String> getMethods()
        {
            return methods;
        }
    }
}
