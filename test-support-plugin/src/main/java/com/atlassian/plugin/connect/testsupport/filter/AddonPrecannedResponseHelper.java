package com.atlassian.plugin.connect.testsupport.filter;

public interface AddonPrecannedResponseHelper
{
    void queuePrecannedResponse(String requiredPath, int statusCode);
}
