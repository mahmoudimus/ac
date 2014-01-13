    package com.atlassian.plugin.connect.plugin.scopes;

    import com.atlassian.plugin.connect.api.scopes.ScopeName;
    import com.atlassian.plugin.connect.plugin.scopes.beans.AddOnScopeBean;
    import com.atlassian.plugin.connect.plugin.scopes.beans.AddOnScopeBeans;
    import com.atlassian.plugin.connect.plugin.util.StreamUtil;
    import com.google.common.base.Function;
    import com.google.gson.GsonBuilder;

    import javax.annotation.Nonnull;
    import javax.annotation.Nullable;
    import java.io.IOException;
    import java.io.InputStream;
    import java.util.*;

    import static com.google.common.collect.Collections2.transform;

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
        return buildFor("confluence");
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
        return buildFor("jira");
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
        Collection<AddOnScope> scopes = new ArrayList<AddOnScope>();
        String scopesFileResourceName = resourceLocation(product);
        AddOnScopeBeans scopeBeans = parseScopeBeans(scopesFileResourceName);

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

        String rawJson = StreamUtil.getStringFromInputStream(inputStream);
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
     * @return the {@link AddOnScope}s referenced by the {@link String}s
     * @throws IllegalArgumentException if any of the scopeKeys do not appear amongst the static scopes
     */
    public static Collection<AddOnScope> dereference(Collection<AddOnScope> scopes, @Nonnull final Collection<ScopeName> scopeKeys)
    {
        // avoid loading scopes from file if unnecessary
        if (scopeKeys.isEmpty())
        {
            return Collections.<AddOnScope>emptySet();
        }

        final Map<ScopeName, AddOnScope> scopeKeyToScope = new HashMap<ScopeName, AddOnScope>(scopes.size());

        for (AddOnScope scope : scopes)
        {
            ScopeName scopeName = ScopeName.valueOf(scope.getKey());

            if (scopeKeyToScope.containsKey(scopeName))
            {
                throw new IllegalArgumentException(String.format("Scope name '%s' is specified multiple times.", scope.getKey()));
            }

            scopeKeyToScope.put(scopeName, scope);
        }

        if (!scopeKeyToScope.keySet().containsAll(scopeKeys))
        {
            Set<ScopeName> badKeys = new HashSet<ScopeName>(scopeKeys);
            badKeys.removeAll(scopeKeyToScope.keySet());
            throw new IllegalArgumentException(String.format("Scope keys %s do not exist. Valid values are: %s.", badKeys, scopeKeyToScope.keySet()));
        }

        return transform(scopeKeys, new Function<ScopeName, AddOnScope>()
        {
            @Override
            public AddOnScope apply(@Nullable ScopeName scopeKey)
            {
                if (null == scopeKey)
                {
                    return null;
                }

                return scopeKeyToScope.get(scopeKey);
            }
        });
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
