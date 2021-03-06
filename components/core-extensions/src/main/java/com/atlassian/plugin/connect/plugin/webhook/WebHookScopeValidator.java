package com.atlassian.plugin.connect.plugin.webhook;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;


/**
 * Validates that the descriptor requests the appropriate scopes for defined webhooks.
 *
 * @since 1.0
 */
@Named("web-hook-scopes-validator")
public class WebHookScopeValidator {
    private final WebHookScopeService webHookScopeService;

    @Inject
    public WebHookScopeValidator(final WebHookScopeService webHookScopeService) {
        this.webHookScopeService = webHookScopeService;
    }

    public void validate(final ShallowConnectAddonBean addon, List<WebHookModuleBean> webhooks) throws ConnectModuleValidationException {
        if (webhooks != null) {
            for (WebHookModuleBean webhook : webhooks) {
                final ScopeName requiredScope = webHookScopeService.getRequiredScope(webhook.getEvent());

                if (!Iterables.any(addon.getScopes(), new ImpliedScopePredicate(requiredScope))) {
                    String exceptionMessage = String.format("Add-on '%s' requests web hook '%s' but not the '%s' scope "
                                    + "required to receive it. Please request this scope in your descriptor.", addon.getKey(),
                            webhook.getEvent(), requiredScope);
                    throw new ConnectModuleValidationException(addon, new WebHookModuleMeta(), exceptionMessage,
                            "connect.install.error.missing.scope", requiredScope);
                }
            }
        }
    }

    private static class ImpliedScopePredicate implements Predicate<ScopeName> {
        private final ScopeName requiredScope;

        private ImpliedScopePredicate(final ScopeName requiredScope) {
            this.requiredScope = requiredScope;
        }

        @Override
        public boolean apply(final ScopeName requestedScope) {
            return requestedScope == requiredScope || requestedScope.getImplied().contains(requiredScope);
        }
    }
}
