package com.atlassian.plugin.connect.bitbucket;

import javax.inject.Inject;

import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.BitbucketImport;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.bitbucket.license.LicenseService;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.user.UserAdminService;
import com.atlassian.bitbucket.user.UserService;

/**
 * This class does nothing but is here to centralize the Stash component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings("ALL")
@BitbucketComponent
public class BitbucketImports
{
    @Inject
    public BitbucketImports(
            @BitbucketImport LicenseService licenseService,
            @BitbucketImport AuthenticationContext authenticationContext,
            @BitbucketImport PermissionService permissionService,
            @BitbucketImport UserAdminService userAdminService,
            @BitbucketImport UserService userService,
            @BitbucketImport WebFragmentHelper webFragmentHelper)
    {
    }
}
