package com.atlassian.plugin.remotable.api.service.http;

/**
 * Exception thrown by the XML-RPC library in case of a fault response. The exception is thrown
 * only if the call was successfully made but the response contained a fault message. If a call
 * could not be made due to a local problem (if an argument could not be serialized or if there
 * was a network problem) an XmlRpcException is thrown instead.
 *
 * @author Greger Olsson
 */
public class XmlRpcFault extends XmlRpcException
{
    /**
     * Creates a new exception with the supplied message and error code. The message and error
     * code values are those returned from the remote XML-RPC service.
     *
     * @param message The exception message.
     */

    public XmlRpcFault(int errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code reported by the remote XML-RPC service.
     *
     * @return the error code reported by the XML-RPC service.
     */

    public int getErrorCode()
    {
        return errorCode;
    }

    /**
     * The exception error code. See XML-RPC specification.
     */
    public final int errorCode;
}
