package com.atlassian.plugin.connect.plugin.scopes;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddOnScopeMatcher extends BaseMatcher<AddOnScope>
{
    private final String key;
    private final Collection<AddOnScopeApiPath> paths;

    public AddOnScopeMatcher(String key, Collection<AddOnScopeApiPath> paths)
    {
        this.key = checkNotNull(key);
        this.paths = checkNotNull(paths);
    }

    @Override
    public boolean matches(Object item)
    {
        if (!(item instanceof AddOnScope))
        {
            System.out.println("Not an AddOnScope: " + item);
            return false;
        }

        AddOnScope scope = (AddOnScope) item;

        if (!key.equals(scope.getKey()))
        {
            System.out.println(key + " != " + scope.getKey());
        }

        if (!paths.equals(scope.getPaths()))
        {
            System.out.println(paths.toString() + " != " + scope.getPaths());
        }

        return key.equals(scope.getKey()) && paths.equals(scope.getPaths());
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(AddOnScope.class.getSimpleName());
        description.appendText("[key=");
        description.appendText(key);
        description.appendText(",paths=");
        description.appendText(paths.toString());
        description.appendText("]");
    }
}