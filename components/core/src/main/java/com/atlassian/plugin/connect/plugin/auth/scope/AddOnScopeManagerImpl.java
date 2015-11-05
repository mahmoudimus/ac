package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.AddOnScopeApiPath;
import com.atlassian.plugin.connect.spi.scope.ApiScope;
import com.atlassian.plugin.connect.spi.scope.helper.RestApiScopeHelper;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@Component
@ExportAsDevService
public final class AddOnScopeManagerImpl implements AddOnScopeManager
{
    private final Collection<AddOnScope> allScopes;
    private final AddOnScope addOnPropertyScope;
    private final ConnectAddonAccessor addonAccessor;

    @Autowired
    public AddOnScopeManagerImpl(ScopeService scopeService, ConnectAddonAccessor addonAccessor) throws IOException
    {
        this.allScopes = scopeService.build();
        this.addonAccessor = addonAccessor;
        this.addOnPropertyScope = createAddOnPropertyScope();
    }

    private AddOnScope createAddOnPropertyScope()
    {
        RestApiScopeHelper.RestScope restScope = new RestApiScopeHelper.RestScope("atlassian-connect", Arrays.asList("1", "latest"), "/addons($|/.*)", Arrays.asList("GET", "POST", "PUT", "DELETE"), true);

        ArrayList<AddOnScopeApiPath> paths = new ArrayList<>();
        paths.add(new AddOnScopeApiPath.RestApiPath(Collections.singleton(restScope)));

        return new AddOnScope("ADD_ON_PROPERTIES", paths);
    }

    @Override
    public boolean isRequestInApiScope(HttpServletRequest request, String addonKey)
    {
        return any(getApiScopesForPlugin(addonKey), new IsInApiScopePredicate(request));
    }

    private Iterable<? extends ApiScope> getApiScopesForPlugin(String addonKey)
    {
        return Iterables.concat(StaticAddOnScopes.dereference(allScopes, getScopeReferences(addonKey)), Collections.singleton(addOnPropertyScope));
    }

    private Set<ScopeName> getScopeReferences(String pluginKey)
    {
        Optional<ConnectAddonBean> optionalAddon = addonAccessor.getAddon(pluginKey);
        if (!optionalAddon.isPresent())
        {
            throw new IllegalStateException(String.format("The Connect Add-on Registry has no descriptor for add-on '%s' and therefore we cannot compute its scopes!", pluginKey));
        }
        return optionalAddon.get().getScopes();
    }

    private static final class IsInApiScopePredicate implements Predicate<ApiScope>
    {
        private final HttpServletRequest request;

        public IsInApiScopePredicate(HttpServletRequest request)
        {
            this.request = checkNotNull(request);
        }

        @Override
        public boolean apply(ApiScope scope)
        {
            return null != scope && scope.allow(request);
        }
    }

}
