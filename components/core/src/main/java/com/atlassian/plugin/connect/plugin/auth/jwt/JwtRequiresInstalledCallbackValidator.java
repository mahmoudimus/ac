package com.atlassian.plugin.connect.plugin.auth.jwt;

import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.descriptor.ShallowConnectAddonBeanValidator;

import javax.inject.Named;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationType.JWT;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * If the add-on has requested JWT authentication, this validator checks that the add-on has also registered an
 * installed callback.
 *
 * @since 1.0
 */
@Named("jwt-requires-installed-callback-validator")
public class JwtRequiresInstalledCallbackValidator implements ShallowConnectAddonBeanValidator {

    @Override
    public void validate(final ShallowConnectAddonBean addon) throws InvalidDescriptorException {
        if (addon.getAuthentication().getType() == JWT && !hasInstalledCallback(addon)) {
            throw new InvalidDescriptorException("The add-on (" + addon.getKey() + ") requested authentication "
                    + "but did not specify an installed lifecycle callback in its descriptor.",
                    "connect.install.error.auth.with.no.installed.callback");
        }
    }

    private boolean hasInstalledCallback(final ShallowConnectAddonBean addon) {
        return addon.getLifecycle() != null && !isNullOrEmpty(addon.getLifecycle().getInstalled());
    }
}
