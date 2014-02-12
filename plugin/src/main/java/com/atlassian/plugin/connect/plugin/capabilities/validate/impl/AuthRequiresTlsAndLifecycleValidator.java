package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationType.JWT;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * If the add-on requests authentication, this validator checks that: <ol> <li>the add-on has registered an installed
 * callback</li> <li>the base url starts with https:// (unless running in dev mode)</li> </ol>
 *
 * @since 1.0
 */
@Named("auth-tls-and-lifecycle-validator")
public class AuthRequiresTlsAndLifecycleValidator implements AddOnBeanValidator
{
    private final IsDevModeService isDevModeService;

    @Autowired
    public AuthRequiresTlsAndLifecycleValidator(final IsDevModeService isDevModeService)
    {
        this.isDevModeService = isDevModeService;
    }

    @Override
    public void validate(final ConnectAddonBean addon) throws InvalidDescriptorException
    {
        if (addon.getAuthentication() != null && addon.getAuthentication().getType() == JWT)
        {
            if (!isDevModeService.isDevMode() && !addon.getBaseUrl().toLowerCase().startsWith("https:"))
            {
                String message = String.format("Cannot issue auth callback except via HTTPS. Current base URL = '%s'",
                        addon.getBaseUrl());
                throw new InvalidDescriptorException(message, "connect.install.error.auth.with.no.tls");
            }

            if (addon.getLifecycle() == null || isNullOrEmpty(addon.getLifecycle().getInstalled()))
            {
                throw new InvalidDescriptorException("The add-on (" + addon.getKey() + ") requested authentication "
                        + "but did not specify an installed lifecycle callback in its descriptor.",
                        "connect.install.error.auth.with.no.installed.callback");
            }
        }
    }
}
