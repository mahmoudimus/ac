package com.atlassian.plugin.connect.testsupport.filter;

import java.util.LinkedList;
import java.util.Queue;

import com.atlassian.fugue.Option;

public class DefaultAddonPrecannedResponseHelper implements AddonPrecannedResponseHelper
{
    private final Queue<PrecannedResponse> precannedResponseQueue = new LinkedList<PrecannedResponse>();


    public void queuePrecannedResponse(String requiredPath, int statusCode)
    {
        precannedResponseQueue.add(new PrecannedResponse(requiredPath, statusCode));
    }

    @Override
    public Option<PrecannedResponse> poll()
    {
        return Option.option(precannedResponseQueue.poll());
    }
}
