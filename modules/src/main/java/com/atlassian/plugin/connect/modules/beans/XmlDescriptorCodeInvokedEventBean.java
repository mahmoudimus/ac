package com.atlassian.plugin.connect.modules.beans;

import static com.google.common.base.Preconditions.checkNotNull;

public class XmlDescriptorCodeInvokedEventBean
{
    private final String addOnKey;
    private final StackTraceElement[] stackTrace;

    public XmlDescriptorCodeInvokedEventBean(String addOnKey, StackTraceElement[] stackTrace)
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
