package com.atlassian.plugin.connect.api.auth;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.plugin.connect.api.auth.scope.AddonKeyExtractor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A component for checking if a user or a current add-on has access to some other add-ons data
 */
@PublicApi
public class AddonDataAccessChecker
{
    private final UserManager userManager;
    private final AddonKeyExtractor addonKeyExtractor;

    @Autowired
    public AddonDataAccessChecker(final UserManager userManager, final AddonKeyExtractor addonKeyExtractor)
    {
        this.userManager = userManager;
        this.addonKeyExtractor = addonKeyExtractor;
    }

    /**
     * Will return true iff. the current user is sysadmin or the current addon is the one identified by the {@code addonKey} 
     */
    public boolean hasAccessToAddon(AuthenticationData authenticationData, String addonKey)
    {
        return authenticationData.accept(new AuthenticationData.AuthenticationDetailsVisitor<Boolean>()
        {
            @Override
            public Boolean visit(final AuthenticationData.Request authenticationBy)
            {
                HttpServletRequest request = authenticationBy.getRequest();
                return hasAccessToAddon(userManager.getRemoteUser(request), addonKeyExtractor.extractClientKey(request), addonKey);
            }

            @Override
            public Boolean visit(final AuthenticationData.AddonKey authenticationBy)
            {
                return hasAccessToAddon(null, authenticationBy.getAddonKey(), addonKey);
            }

            @Override
            public Boolean visit(final AuthenticationData.User authenticationBy)
            {
                return hasAccessToAddon(authenticationBy.getUser(), null, addonKey);
            }
        });
    }
    
    /**
     * Will return true iff. user is sysadmin or currentAddOnKey equals addonKey
     */
    public boolean hasAccessToAddon(@Nullable final UserProfile user, @Nullable final String currentAddOnKey, String addonKey)
    {
        return currentAddOnKey != null && currentAddOnKey.equals(addonKey) || user != null && userManager.isSystemAdmin(user.getUserKey());
    }
}
