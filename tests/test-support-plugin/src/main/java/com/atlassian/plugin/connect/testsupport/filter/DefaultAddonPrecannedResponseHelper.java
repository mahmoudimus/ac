package com.atlassian.plugin.connect.testsupport.filter;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class DefaultAddonPrecannedResponseHelper implements AddonPrecannedResponseHelper
{
    private final Queue<PrecannedResponse> precannedResponseQueue = new LinkedList<PrecannedResponse>();


    public void queuePrecannedResponse(String requiredPath, int statusCode)
    {
        precannedResponseQueue.add(new PrecannedResponse(requiredPath, statusCode));
    }

    @Override
    public Optional<PrecannedResponse> poll()
    {
        return Optional.ofNullable(precannedResponseQueue.poll());
    }
}
