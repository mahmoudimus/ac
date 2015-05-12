package com.atlassian.plugin.connect.plugin.imports;

import javax.inject.Inject;

import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.StashImport;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.stash.license.LicenseService;
import com.atlassian.stash.user.PermissionService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.UserAdminService;
import com.atlassian.stash.user.UserService;

/**
 * This class does nothing but is here to centralize the Stash component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings("ALL")
@StashComponent
public class StashImports
{
    @Inject
    public StashImports(
            @StashImport LicenseService licenseService,
            @StashImport StashAuthenticationContext stashAuthenticationContext,
            @StashImport PermissionService permissionService,
            @StashImport UserAdminService userAdminService,
            @StashImport UserService userService,
            @StashImport WebFragmentHelper webFragmentHelper)
    {
    }
}
