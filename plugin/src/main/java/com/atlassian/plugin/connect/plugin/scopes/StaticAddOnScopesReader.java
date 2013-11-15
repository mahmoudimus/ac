package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.plugin.scopes.beans.AddOnScopeBean;
import com.atlassian.plugin.connect.plugin.scopes.beans.AddOnScopeBeans;
import com.google.gson.GsonBuilder;
import com.opensymphony.util.FileUtils;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class StaticAddOnScopesReader
{
    /**
     * Parse static resources into the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s.
     * Reads Confluence configuration.
     * @return the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s
     * @throws IOException if the static resources file could not be read
     */
    public static Collection<AddOnScope> buildForConfluence() throws IOException
    {
        return buildFor("confluence");
    }

    /**
     * Parse static resources into the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s.
     * Configured for Confluence.
     * @param product product name in lower case e.g. "confluence" or "jira"
     * @return the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s
     * @throws IOException if the static resources file could not be read
     */
    static Collection<AddOnScope> buildFor(String product) throws IOException
    {
        Collection<AddOnScope> scopes = new ArrayList<AddOnScope>();
        String rawJson = FileUtils.readFile(new DefaultResourceLoader().getResource(resourceLocation(product)).getFile());
        AddOnScopeBeans scopeBeans = new GsonBuilder().create().fromJson(rawJson, AddOnScopeBeans.class);

        for (AddOnScopeBean scopeBean : scopeBeans.getScopes())
        {
            AddOnScopeApiPathBuilder pathsBuilder = new AddOnScopeApiPathBuilder();

            for (AddOnScopeBean.RestPathBean restPathBean : scopeBean.getRestPaths())
            {
                pathsBuilder.withRestPaths(restPathBean.getName(), restPathBean.getBasePaths(), restPathBean.getVersions(), restPathBean.getMethods());
            }

            scopes.add(new AddOnScope(scopeBean.getKey(), pathsBuilder.build()));
        }

        return scopes;
    }

    /**
     * @param product product name in lower case e.g. "confluence" or "jira"
     * @return resource location e.g. "classpath:/some/file"
     */
    private static String resourceLocation(String product)
    {
        return "classpath:/com/atlassian/connect/scopes." + product + ".json";
    }
}
