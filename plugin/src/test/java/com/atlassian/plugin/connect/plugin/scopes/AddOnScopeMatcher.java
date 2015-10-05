package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.AddOnScopeApiPath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddOnScopeMatcher extends BaseMatcher<AddOnScope>
{

    private static final Logger log = LoggerFactory.getLogger(AddOnScopeMatcher.class);

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
            log.debug("Not an AddOnScope: " + item);
            return false;
        }

        AddOnScope scope = (AddOnScope) item;

        if (!key.equals(scope.getKey()))
        {
            log.debug(key + " != " + scope.getKey());
        }

        if (!paths.equals(scope.getPaths()))
        {
            log.debug(paths.toString() + " != " + scope.getPaths());
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