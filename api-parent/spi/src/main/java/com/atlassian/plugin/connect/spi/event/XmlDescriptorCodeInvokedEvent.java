package com.atlassian.plugin.connect.spi.event;

import com.atlassian.analytics.api.annotations.EventName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fired when code used in processing the deprecated Connect XML descriptor format is invoked.
 */
@EventName("connect.xmldescriptor.code.invoked")
public class XmlDescriptorCodeInvokedEvent
{
    private final String addOnKey;
    private final StackTraceElement[] stackTrace;

    public XmlDescriptorCodeInvokedEvent(String addOnKey, StackTraceElement[] stackTrace)
    {
        this.addOnKey = checkNotNull(addOnKey);
        this.stackTrace = checkNotNull(stackTrace);
    }

    public String getAddOnKey()
    {
        return addOnKey;
    }

    public StackTraceElement[] getStackTrace()
    {
        return stackTrace;
    }
}
