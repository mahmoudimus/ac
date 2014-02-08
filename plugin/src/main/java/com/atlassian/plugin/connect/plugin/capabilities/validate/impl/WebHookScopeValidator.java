package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.WebHookScopeService;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;


/**
 * Validates that the descriptor requests the appropriate scopes for defined webhooks.
 *
 * @since 1.0
 */
@Named("web-hook-scopes-validator")
public class WebHookScopeValidator implements AddOnBeanValidator
{
    private final WebHookScopeService webHookScopeService;

    @Autowired
    public WebHookScopeValidator(final WebHookScopeService webHookScopeService)
    {
        this.webHookScopeService = webHookScopeService;
    }

    @Override
    public void validate(final Plugin plugin, final ConnectAddonBean addon) throws InvalidDescriptorException
    {
        for (WebHookModuleBean webHookModuleBean : addon.getModules().getWebhooks())
        {
            ScopeName requiredScope = webHookScopeService.getRequiredScope(webHookModuleBean.getEvent());

            if (!addon.getScopes().contains(requiredScope))
            {
                String exceptionMessage = String.format("Add-on '%s' requests web hook '%s' but not the '%s' scope "
                        + "required to receive it. Please request this scope in your descriptor.", plugin.getKey(),
                        webHookModuleBean.getEvent(), requiredScope);
                throw new InvalidDescriptorException(exceptionMessage, "connect.install.error.missing.scope." + requiredScope.name());
            }
        }
    }
}
