package com.atlassian.plugin.connect.spi.scope.helper;

import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBean;
import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBeans;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.AddOnScopeApiPathBuilder;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class AddOnScopeLoadJsonFileHelper
{
    private AddOnScopeLoadJsonFileHelper() {}

    public static void addProductScopesFromFile(Map<ScopeName, AddOnScope> keyToScope, URL urlToScopeResource) throws IOException
    {
        AddOnScopeBeans scopeBeans = parseScopeBeans(urlToScopeResource);

        for (AddOnScopeBean scopeBean : scopeBeans.getScopes())
        {
            constructAndAddScope(keyToScope, urlToScopeResource.getFile(), scopeBeans, scopeBean);
        }
    }

    public static void combineProductScopes(final Map<ScopeName, AddOnScope> source, final Map<ScopeName, AddOnScope> addition)
    {
        //TODO: use Map.merge when we will have Java 8
        for (ScopeName scopeName : addition.keySet())
        {
            AddOnScope additionalScope = addition.get(scopeName);
            if (source.containsKey(scopeName))
            {
                AddOnScope existingScope = source.get(scopeName);
                AddOnScopeApiPathBuilder pathsBuilder = new AddOnScopeApiPathBuilder()
                        .withPaths(existingScope.getPaths())
                        .withPaths(additionalScope.getPaths());

                source.put(scopeName, new AddOnScope(existingScope.getKey(), pathsBuilder.build()));
            }
            else
            {
                source.put(scopeName, additionalScope);
            }
        }
    }

    private static AddOnScopeBeans parseScopeBeans(URL urlToScopeResource) throws IOException
    {
        InputStream inputStream = urlToScopeResource.openStream();

        if (null == inputStream)
        {
            throw new IOException(String.format("Static scopes resource does not exist: '%s'", urlToScopeResource.toString()));
        }

        String rawJson = IOUtils.toString(inputStream, "UTF-8");
        return new GsonBuilder().create().fromJson(rawJson, AddOnScopeBeans.class);
    }

    private static void constructAndAddScope(Map<ScopeName, AddOnScope> keyToScope, String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean)
    {
        checkScopeName(scopeBean); // scope names must be valid
        AddOnScopeApiPathBuilder pathsBuilder = new AddOnScopeApiPathBuilder();
        ScopeName scopeName = ScopeName.valueOf(scopeBean.getKey());
        AddOnScope existingScope = keyToScope.get(scopeName);

        if (null != existingScope)
        {
            pathsBuilder = pathsBuilder.withPaths(existingScope.getPaths());
        }

        addRestPaths(scopesFileResourceName, scopeBeans, scopeBean, pathsBuilder);
        addSoapRpcPaths(scopesFileResourceName, scopeBeans, scopeBean, pathsBuilder);
        addJsonRpcPaths(scopesFileResourceName, scopeBeans, scopeBean, pathsBuilder);
        addXmlRpcPaths(scopesFileResourceName, scopeBeans, scopeBean, pathsBuilder);
        addPaths(scopesFileResourceName, scopeBeans, scopeBean, pathsBuilder);
        keyToScope.put(scopeName, new AddOnScope(scopeBean.getKey(), pathsBuilder.build()));
    }

    private static void addPaths(String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean, AddOnScopeApiPathBuilder pathsBuilder)
    {
        for (String pathKey : scopeBean.getPathKeys())
        {
            boolean found = false;
            int pathIndex = 0;

            for (AddOnScopeBean.PathBean pathBean : scopeBeans.getPaths())
            {
                if (null == pathBean.getKey())
                {
                    throw new IllegalArgumentException(String.format("Path index %d in scopes file '%s' has a null or missing 'key': please add a key", pathIndex, scopesFileResourceName));
                }

                if (pathBean.getKey().equals(pathKey))
                {
                    found = true;
                    pathsBuilder.withPaths(pathBean);
                    break;
                }

                ++pathIndex;
            }

            if (!found)
            {
                throw new IllegalArgumentException(String.format("Path key '%s' in scope '%s' is not the key of any restPath in the JSON scopes file '%s': please correct this typo",
                        pathKey, scopeBean.getKey(), scopesFileResourceName));
            }
        }
    }

    private static void addJsonRpcPaths(String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean, AddOnScopeApiPathBuilder pathsBuilder)
    {
        for (String jsonRpcPathKey : scopeBean.getJsonRpcPathKeys())
        {
            boolean found = false;
            int jsonPathIndex = 0;

            for (AddOnScopeBean.JsonRpcPathBean jsonRpcPathBean : scopeBeans.getJsonRpcPaths())
            {
                if (null == jsonRpcPathBean.getKey())
                {
                    throw new IllegalArgumentException(String.format("JSON path index %d in scopes file '%s' has a null or missing 'key': please add a key", jsonPathIndex, scopesFileResourceName));
                }

                if (jsonRpcPathBean.getKey().equals(jsonRpcPathKey))
                {
                    found = true;
                    pathsBuilder.withJsonRpcResources(jsonRpcPathBean, scopeBean.getMethods());
                    break;
                }

                ++jsonPathIndex;
            }

            if (!found)
            {
                throw new IllegalArgumentException(String.format("JSON path key '%s' in scope '%s' is not the key of any restPath in the JSON scopes file '%s': please correct this typo",
                        jsonRpcPathKey, scopeBean.getKey(), scopesFileResourceName));
            }
        }
    }

    private static void addXmlRpcPaths(String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean, AddOnScopeApiPathBuilder pathsBuilder)
    {
        for (String xmlRpcPathKey : scopeBean.getXmlRpcPathKeys())
        {
            boolean found = false;
            int xmlPathIndex = 0;

            for (AddOnScopeBean.XmlRpcBean xmlRpcBean : scopeBeans.getXmlRpcPaths())
            {
                if (null == xmlRpcBean.getKey())
                {
                    throw new IllegalArgumentException(String.format("XML-RPC path index %d in scopes file '%s' has a null or missing 'key': please add a key", xmlPathIndex, scopesFileResourceName));
                }

                if (xmlRpcBean.getKey().equals(xmlRpcPathKey))
                {
                    found = true;
                    pathsBuilder.withXmlRpcResources(xmlRpcBean);
                    break;
                }

                ++xmlPathIndex;
            }

            if (!found)
            {
                throw new IllegalArgumentException(String.format("XML-RPC path key '%s' in scope '%s' is not the key of any restPath in the JSON scopes file '%s': please correct this typo",
                        xmlRpcPathKey, scopeBean.getKey(), scopesFileResourceName));
            }
        }
    }

    private static void addSoapRpcPaths(String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean, AddOnScopeApiPathBuilder pathsBuilder)
    {
        for (String soapRpcPathKey : scopeBean.getSoapRpcPathKeys())
        {
            boolean found = false;
            int soapPathIndex = 0;

            for (AddOnScopeBean.SoapRpcPathBean soapRpcPathBean : scopeBeans.getSoapRpcPaths())
            {
                if (null == soapRpcPathBean.getKey())
                {
                    throw new IllegalArgumentException(String.format("SOAP path index %d in scopes file '%s' has a null or missing 'key': please add a key", soapPathIndex, scopesFileResourceName));
                }

                if (soapRpcPathBean.getKey().equals(soapRpcPathKey))
                {
                    found = true;
                    pathsBuilder.withSoapRpcResources(soapRpcPathBean, scopeBean.getMethods());
                    break;
                }

                ++soapPathIndex;
            }

            if (!found)
            {
                throw new IllegalArgumentException(String.format("SOAP path key '%s' in scope '%s' is not the key of any restPath in the JSON scopes file '%s': please correct this typo",
                        soapRpcPathKey, scopeBean.getKey(), scopesFileResourceName));
            }
        }
    }

    private static void addRestPaths(String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean, AddOnScopeApiPathBuilder pathsBuilder)
    {
        for (String restPathKey : scopeBean.getRestPathKeys())
        {
            boolean found = false;
            int restPathIndex = 0;

            for (AddOnScopeBean.RestPathBean restPathBean : scopeBeans.getRestPaths())
            {
                if (null == restPathBean.getKey())
                {
                    throw new IllegalArgumentException(String.format("restPath index %d in scopes file '%s' has a null or missing 'key': please add a key", restPathIndex, scopesFileResourceName));
                }

                if (restPathBean.getKey().equals(restPathKey))
                {
                    found = true;
                    pathsBuilder.withRestPaths(restPathBean, scopeBean.getMethods());
                    break;
                }

                ++restPathIndex;
            }

            if (!found)
            {
                throw new IllegalArgumentException(String.format("restPath key '%s' in scope '%s' is not the key of any restPath in the JSON scopes file '%s': please correct this typo",
                        restPathKey, scopeBean.getKey(), scopesFileResourceName));
            }
        }
    }

    private static void checkScopeName(AddOnScopeBean scopeBean)
    {
        try
        {
            ScopeName.valueOf(scopeBean.getKey());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(String.format("Scope name '%s' is not valid. Valid scopes are %s", scopeBean.getKey(), Arrays.asList(ScopeName.values())));
        }
    }
}
