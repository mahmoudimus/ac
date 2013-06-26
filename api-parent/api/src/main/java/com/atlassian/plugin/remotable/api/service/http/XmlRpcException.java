package com.atlassian.plugin.remotable.api.service.http;

/**
 * Exception for xml rpc problems
 */
public class XmlRpcException extends RuntimeException
{
    public XmlRpcException()
    {
        super();
    }

    public XmlRpcException(String message)
    {
        super(message);
    }

    public XmlRpcException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public XmlRpcException(Throwable cause)
    {
        super(cause);
    }
}
