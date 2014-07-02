package com.atlassian.plugin.connect.testsupport.filter;

import com.atlassian.sal.api.user.UserManager;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Record all requests to the fake add-on servlet, throwing errors on any that get this far and do not match.
 * Also see {@link com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterBase}.
 */
public class AddonTestFilter extends AddonTestFilterBase
{
    public AddonTestFilter(AddonTestFilterResults testFilterResults, UserManager userManager,
                           AddonPrecannedResponseHelper addonPrecannedResponseHelper)
    {
        super(testFilterResults, userManager, addonPrecannedResponseHelper);
    }

    @Override
    protected boolean shouldProcess(HttpServletRequest request)
    {
        // process all incoming requests
        return true;
    }

    @Override
    protected void processNonMatch(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException
    {
        // capture all requests and stop; if it doesn't match then something is misconfigured
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
