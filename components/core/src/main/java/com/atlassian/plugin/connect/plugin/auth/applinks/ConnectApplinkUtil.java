package com.atlassian.plugin.connect.plugin.auth.applinks;

import java.util.Optional;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.auth.AuthenticationMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectApplinkUtil {

    private static final Logger log = LoggerFactory.getLogger(ConnectApplinkUtil.class);

    public static Optional<AuthenticationType> getAuthenticationType(ApplicationLink applink) {
        Object authMethod = applink.getProperty(AuthenticationMethod.PROPERTY_NAME);
        if (AuthenticationMethod.JWT.toString().equals(authMethod)) {
            return Optional.of(AuthenticationType.JWT);
        } else if (AuthenticationMethod.NONE.toString().equals(authMethod)) {
            return Optional.of(AuthenticationType.NONE);
        } else if (authMethod != null) {
            log.warn("Unknown authType encountered: " + authMethod);
            return Optional.of(AuthenticationType.NONE);
        }
        return Optional.empty();
    }
}
