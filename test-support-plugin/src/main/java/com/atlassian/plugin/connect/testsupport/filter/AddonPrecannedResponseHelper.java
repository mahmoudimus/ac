package com.atlassian.plugin.connect.testsupport.filter;

import com.atlassian.fugue.Option;

public interface AddonPrecannedResponseHelper
{
    void queuePrecannedResponse(String requiredPath, int statusCode);

    Option<PrecannedResponse> poll();
}
