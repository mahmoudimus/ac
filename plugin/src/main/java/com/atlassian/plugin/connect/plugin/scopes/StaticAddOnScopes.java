package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBean;
import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBeans;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class StaticAddOnScopes
{
    /**
     * Parse static resources into the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s.
     * Reads Confluence configuration.
     * For efficiency call this method sparingly, as repeated calls will all load scopes from file.
     * @return the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s
     * @throws IOException if the static resources file could not be read
     */
    public static Collection<AddOnScope> buildForConfluence() throws IOException
    {
        ArrayList<AddOnScope> addOnScopes = Lists.newArrayList();
        addOnScopes.addAll(buildFor("confluence"));
        addOnScopes.addAll(buildFor("common"));
        return addOnScopes;
    }

    /**
     * Parse static resources into the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s.
     * Reads JIRA configuration.
     * For efficiency call this method sparingly, as repeated calls will all load scopes from file.
     * @return the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s
     * @throws IOException if the static resources file could not be read
     */
    public static Collection<AddOnScope> buildForJira() throws IOException
    {
        ArrayList<AddOnScope> addOnScopes = Lists.newArrayList();
        addOnScopes.addAll(buildFor("jira"));
        addOnScopes.addAll(buildFor("common"));
        return addOnScopes;
    }

    /**
     * Parse static resources into the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s.
     * For efficiency call this method sparingly, as repeated calls will all load scopes from file.
     * @param product product name in lower case e.g. "confluence" or "jira"
     * @return the {@link Collection} of {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s
     * @throws IOException if the static resources file could not be read
     */
    static Collection<AddOnScope> buildFor(String product) throws IOException
    {
        String scopesFileResourceName = resourceLocation(product);
        AddOnScopeBeans scopeBeans = parseScopeBeans(scopesFileResourceName);
        return buildFromBeans(scopeBeans, scopesFileResourceName);
    }

    private static Collection<AddOnScope> buildFromBeans(AddOnScopeBeans scopeBeans, String scopesFileResourceName)
    {
        Collection<AddOnScope> scopes = new ArrayList<AddOnScope>();

        for (AddOnScopeBean scopeBean : scopeBeans.getScopes())
        {
            constructAndAddScope(scopes, scopesFileResourceName, scopeBeans, scopeBean);
        }

        return scopes;
    }

    private static AddOnScopeBeans parseScopeBeans(String scopesFileResourceName) throws IOException
    {
        InputStream inputStream = StaticAddOnScopes.class.getResourceAsStream(scopesFileResourceName);

        if (null == inputStream)
        {
            throw new IOException(String.format("Static scopes resource does not exist: '%s'", scopesFileResourceName));
        }

        String rawJson = IOUtils.toString(inputStream, "UTF-8");
        return new GsonBuilder().create().fromJson(rawJson, AddOnScopeBeans.class);
    }

    private static void constructAndAddScope(Collection<AddOnScope> scopes, String scopesFileResourceName, AddOnScopeBeans scopeBeans, AddOnScopeBean scopeBean)
    {
        checkScopeName(scopeBean); // scope names must be valid
        AddOnScopeApiPathBuilder pathsBuilder = new AddOnScopeApiPathBuilder();

        for (String restPathKey : scopeBean.getRestPaths())
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

        scopes.add(new AddOnScope(scopeBean.getKey(), pathsBuilder.build()));
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

    /**
     * Turn lightweight references to scopes into the scopes themselves.
     * @param scopes {@link AddOnScope}s previously read from static configuration
     * @param scopeKeys lightweight references to scopes
     * @return the {@link AddOnScope}s referenced by the {@link ScopeName}s
     * @throws IllegalArgumentException if any of the scopeKeys is null
     */
    public static Collection<AddOnScope> dereference(Collection<AddOnScope> scopes, @Nonnull final Collection<ScopeName> scopeKeys)
    {
        // avoid loading scopes from file if unnecessary
        if (scopeKeys.isEmpty())
        {
            return Collections.emptySet();
        }

        for (ScopeName scopeKey : scopeKeys)
        {
            if (null == scopeKey)
            {
                throw new IllegalArgumentException("Scope keys must not contain null");
            }
        }

        return Lists.newArrayList(Iterables.filter(scopes, new Predicate<AddOnScope>()
        {
            @Override
            public boolean apply(@Nullable AddOnScope scope)
            {
                ScopeName scopeName = ScopeName.valueOf(scope.getKey());
                return scopeKeys.contains(scopeName);
            }
        }));
    }

    /**
     * @param product product name in lower case e.g. "confluence" or "jira"
     * @return resource location e.g. "classpath:/some/file"
     */
    private static String resourceLocation(String product)
    {
        return "/com/atlassian/connect/scopes." + product + ".json";
    }
}
