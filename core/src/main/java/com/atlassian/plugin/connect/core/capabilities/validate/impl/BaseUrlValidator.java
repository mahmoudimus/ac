package com.atlassian.plugin.connect.core.capabilities.validate.impl;

import com.atlassian.plugin.connect.api.service.IsDevModeService;
import com.atlassian.plugin.connect.core.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.core.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Validates that the descriptor's baseUrl is absolute, and uses an HTTPS scheme if the host application is not running
 * in dev mode.
 *
 * @since 1.0
 */
@Named("base-url-validator")
public class BaseUrlValidator implements AddOnBeanValidator
{
    private final IsDevModeService isDevModeService;

    @Inject
    public BaseUrlValidator(final IsDevModeService isDevModeService)
    {
        this.isDevModeService = isDevModeService;
    }

    @Override
    public void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException
    {
        // if the baseUrl does not exist or is not a valid URI then schema validation fails and the code never gets here,
        // so there's no need to check for null, empty-string or not-a-URI
        URI baseUrl = URI.create(addonBean.getBaseUrl());
        if (null == baseUrl.getScheme())
        {
            String message = String.format("Add-on '%s' specifies a 'baseUrl' without a HTTP scheme: please add a scheme (e.g. 'https').", addonBean.getKey());
            throw new InvalidDescriptorException(message, "connect.install.error.base_url.no_scheme");
        }

        if (!isDevModeService.isDevMode() && !baseUrl.getScheme().equalsIgnoreCase("https"))
        {
            String message = String.format("Add-on '%s' specifies a 'baseUrl' with the scheme 'http'. Add-ons running "
                    + "in production must be hosted on a domain protected with 'https'", addonBean.getKey());
            throw new InvalidDescriptorException(message, "connect.install.error.base_url.no_tls");
        }
    }
}
