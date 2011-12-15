package com.atlassian.labs.remoteapps.webhook;

import org.apache.poi.hssf.record.formula.functions.T;

/**
 *
 */
public interface EventSerializer
{
    Object getEvent();
    String getJson() throws EventSerializationException;
}
