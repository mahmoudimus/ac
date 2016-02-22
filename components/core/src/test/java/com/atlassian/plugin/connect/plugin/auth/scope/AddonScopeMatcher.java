package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScopeApiPath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddonScopeMatcher extends BaseMatcher<AddonScope> {

    private static final Logger log = LoggerFactory.getLogger(AddonScopeMatcher.class);

    private final String key;
    private final Collection<AddonScopeApiPath> paths;

    public AddonScopeMatcher(String key, Collection<AddonScopeApiPath> paths) {
        this.key = checkNotNull(key);
        this.paths = checkNotNull(paths);
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof AddonScope)) {
            log.debug("Not an AddonScope: " + item);
            return false;
        }

        AddonScope scope = (AddonScope) item;

        if (!key.equals(scope.getKey())) {
            log.debug(key + " != " + scope.getKey());
        }

        if (!paths.equals(scope.getPaths())) {
            log.debug(paths.toString() + " != " + scope.getPaths());
        }

        return key.equals(scope.getKey()) && paths.equals(scope.getPaths());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(AddonScope.class.getSimpleName());
        description.appendText("[key=");
        description.appendText(key);
        description.appendText(",paths=");
        description.appendText(paths.toString());
        description.appendText("]");
    }
}
