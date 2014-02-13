package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;

import javax.inject.Named;
import java.net.URI;

/**
 * Validates that the descriptor's baseUrl exists with a legal value.
 *
 * @since 1.0
 */
@Named("base-url-validator")
public class BaseUrlValidator implements AddOnBeanValidator
{
    @Override
    public void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException
    {
        // if the baseUrl does not exist or is not a valid URI then schema validation fails and the code never gets here,
        // so there's no need to check for null, empty-string or not-a-URI
        if (null == URI.create(addonBean.getBaseUrl()).getScheme())
        {
            String message = String.format("Add-on '%s' specifies a 'baseUrl' without a HTTP scheme: please add a scheme (e.g. 'https').", addonBean.getKey());
            throw new InvalidDescriptorException(message, "connect.install.error.base_url.no_scheme");
        }
    }
}
