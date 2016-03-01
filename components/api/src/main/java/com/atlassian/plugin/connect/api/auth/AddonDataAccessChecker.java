package com.atlassian.plugin.connect.api.auth;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface AddonDataAccessChecker {
    boolean hasAccessToAddon(AuthenticationData authenticationData, String addonKey);
}
